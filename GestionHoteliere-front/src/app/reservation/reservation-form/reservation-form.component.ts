import { Component, Input, OnInit, Inject, Optional } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import {
  ReservationService,
  ReservationChambreRequest,
  ReservationChambreQuoteDTO,
  ReservationChambreOptionRequestDTO,
  ChambreOption
} from 'src/app/services/reservation.service';
import { catchError } from 'rxjs/operators';
import { throwError } from 'rxjs';

type Mode = 'chambre' | 'option' | 'service';

@Component({
  selector: 'app-reservation-form',
  templateUrl: './reservation-form.component.html',
})
export class ReservationFormComponent implements OnInit {

  /** Mode : 'chambre' | 'option' | 'service' */
  @Input() type!: Mode;
  @Input() chambreId?: number;
  @Input() optionId?: number;
  @Input() serviceId?: number;

  form!: FormGroup;

  /** Fallback simple */
  disponibilites = new Set<string>();
  /** Source prioritaire (calendrier réservable) */
  private allowed = new Set<string>();

  options: ChambreOption[] = [];
  loading = false;
  error?: string;
  quote?: ReservationChambreQuoteDTO;

  alwaysTrue = (_: Date | null) => true;

  constructor(
    private fb: FormBuilder,
    private reservationService: ReservationService,
    @Optional() public dialogRef?: MatDialogRef<ReservationFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data?: any
  ) {
    if (data) {
      this.type = data.type;
      this.chambreId = data.chambreId;
      this.optionId = data.optionId;
      this.serviceId = data.serviceId;
    }
  }

  ngOnInit(): void {
    this.buildFormByMode();

    if (this.chambreId) {
      // 1) Fallback “disponibilités”
      this.reservationService.getDisponibilites(this.chambreId).subscribe({
        next: (dates) => {
          const set = new Set(dates ?? []);
          this.disponibilites = set;
          if (this.allowed.size === 0) this.allowed = set;
        },
        error: () => { this.disponibilites = new Set(); }
      });

      // 2) Calendrier prioritaire
      this.reservationService.getCalendar(this.chambreId).subscribe({
        next: (cal) => { if (Array.isArray(cal) && cal.length) this.allowed = new Set(cal); },
        error: () => { /* on garde le fallback */ }
      });

      // 3) Changement de début => on reset fin + devis
      this.form.get('dateDebut')?.valueChanges.subscribe(() => {
        this.form.get('dateFin')?.reset();
        this.quote = undefined;
      });

      // 4) Options liées à la chambre
      this.reservationService.getOptionsByChambre(this.chambreId).subscribe({
        next: (opts) => (this.options = opts),
        error: () => (this.options = [])
      });
    }
  }

  private buildFormByMode() {
    if (this.type === 'chambre' || this.type === 'option') {
      this.form = this.fb.group({
        dateDebut: [null, Validators.required],
        dateFin:   [null, Validators.required],
        nbAdultes: [1, [Validators.required, Validators.min(1)]],
        nbEnfants: [0, [Validators.min(0)]],
        nbChambres:[1, [Validators.required, Validators.min(1)]],
      });
    } else {
      this.form = this.fb.group({
        date:         [null, Validators.required],
        heure:        ['', Validators.required],
        participants: [1, [Validators.required, Validators.min(1)]],
      });
    }
  }

  // ===== utils dates (sans décalage tz) =====
  private pad2(n: number) { return n.toString().padStart(2, '0'); }
  private ymd(d: Date): string {
    return `${d.getFullYear()}-${this.pad2(d.getMonth()+1)}-${this.pad2(d.getDate())}`;
  }
  private addDays(d: Date, n: number): Date { const x = new Date(d); x.setDate(x.getDate() + n); return x; }

  private toLocalDateTimeSeconds(d: Date): string {
    return `${d.getFullYear()}-${this.pad2(d.getMonth()+1)}-${this.pad2(d.getDate())}` +
           `T${this.pad2(d.getHours())}:${this.pad2(d.getMinutes())}:${this.pad2(d.getSeconds())}`;
  }
  private isIntervalValid(): boolean {
    const { dateDebut, dateFin } = this.form.value;
    if (!dateDebut || !dateFin) return false;
    return new Date(dateFin) > new Date(dateDebut);
  }

  /** Vérifie que tous les jours [start … end-1] existent dans allowed (fin exclusive = check‑out) */
  private isRangeAvailable(start: Date, end: Date): boolean {
    if (!start || !end || end <= start) return false;
    let cur = new Date(start);
    const last = this.addDays(end, -1);
    while (cur <= last) {
      if (!this.allowed.has(this.ymd(cur))) return false;
      cur = this.addDays(cur, 1);
    }
    return true;
  }

  // ===== filtres MatDatepicker =====
  dateDisponible = (d: Date | null): boolean => {
    if ((this.type !== 'chambre' && this.type !== 'option') || !d) return true;
    return this.allowed.has(this.ymd(d));
  };

  dateFinDisponible = (d: Date | null): boolean => {
    if ((this.type !== 'chambre' && this.type !== 'option') || !d) return true;
    const start = this.form?.value?.dateDebut ? new Date(this.form.value.dateDebut) : null;
    if (!start) return true;
    if (d <= start) return false;
    // exiger la continuité complète de la plage
    return this.isRangeAvailable(start, d);
  };

  // ===== Devis =====
  doQuote() {
    this.error = undefined;
    this.quote = undefined;

    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (!this.isIntervalValid()) { this.error = 'La date de fin doit être après la date de début.'; return; }

    if (this.type === 'chambre' || this.type === 'option') {
      const start = new Date(this.form.value.dateDebut);
      const end   = new Date(this.form.value.dateFin);
      if (!this.isRangeAvailable(start, end)) {
        this.error = 'Plage indisponible : au moins une nuit est déjà réservée.';
        return;
      }
    }

    const start = new Date(this.form.value.dateDebut);
    const end   = new Date(this.form.value.dateFin);
    this.loading = true;

    if (this.optionId) {
      const body: ReservationChambreOptionRequestDTO = {
        optionId: this.optionId,
        dateDebut: this.toLocalDateTimeSeconds(start),
        dateFin:   this.toLocalDateTimeSeconds(end),
        nbAdultes: this.form.value.nbAdultes,
        nbEnfants: this.form.value.nbEnfants ?? 0,
        nbChambres: this.form.value.nbChambres ?? 1,
      };
      this.reservationService.quoteChambreOption(body).pipe(
        catchError(err => { this.error = err?.error?.message || 'Erreur lors du devis.'; this.loading = false; return throwError(() => err); })
      ).subscribe(q => { this.quote = q; this.loading = false; });
      return;
    }

    if (!this.chambreId) { this.error = 'chambreId manquant.'; this.loading = false; return; }
    const dto: ReservationChambreRequest = {
      chambreId:  this.chambreId,
      dateDebut:  this.toLocalDateTimeSeconds(start),
      dateFin:    this.toLocalDateTimeSeconds(end),
      nbAdultes:  this.form.value.nbAdultes,
      nbEnfants:  this.form.value.nbEnfants ?? 0,
      nbChambres: this.form.value.nbChambres,
    };
    this.reservationService.quoteChambre(dto).pipe(
      catchError(err => { this.error = err?.error?.message || 'Erreur lors du devis.'; this.loading = false; return throwError(() => err); })
    ).subscribe(q => { this.quote = q; this.loading = false; });
  }

  // ===== Soumission =====
  submit() {
    this.error = undefined;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    if (!this.isIntervalValid()) { this.error = 'La date de fin doit être après la date de début.'; return; }

    if (this.type === 'chambre' || this.type === 'option') {
      const start = new Date(this.form.value.dateDebut);
      const end   = new Date(this.form.value.dateFin);
      if (!this.isRangeAvailable(start, end)) {
        this.error = 'Plage indisponible : au moins une nuit est déjà réservée.';
        return;
      }
    }

    if (this.optionId) {
      const body: ReservationChambreOptionRequestDTO = {
        optionId: this.optionId,
        dateDebut: this.toLocalDateTimeSeconds(new Date(this.form.value.dateDebut)),
        dateFin:   this.toLocalDateTimeSeconds(new Date(this.form.value.dateFin)),
        nbAdultes: this.form.value.nbAdultes,
        nbEnfants: this.form.value.nbEnfants ?? 0,
        nbChambres: this.form.value.nbChambres ?? 1,
      };
      this.loading = true;
      this.reservationService.createChambreOption(body).pipe(
        catchError(err => { alert(err.error?.message || 'Erreur réservation (option).'); this.loading = false; return throwError(() => err); })
      ).subscribe(res => {
        alert(`Réservé (option) ! Total: ${res.total ?? res.totalAmount} ${res.currency ?? 'TND'}`);
        this.loading = false;
        if (this.dialogRef) this.dialogRef.close('confirmed');
      });
      return;
    }

    if (!this.chambreId) { this.error = 'chambreId manquant.'; return; }
    const dto: ReservationChambreRequest = {
      chambreId: this.chambreId,
      dateDebut: this.toLocalDateTimeSeconds(new Date(this.form.value.dateDebut)),
      dateFin:   this.toLocalDateTimeSeconds(new Date(this.form.value.dateFin)),
      nbAdultes: this.form.value.nbAdultes,
      nbEnfants: this.form.value.nbEnfants ?? 0,
      nbChambres: this.form.value.nbChambres,
    };
    this.loading = true;
    this.reservationService.createChambre(dto).pipe(
      catchError(err => { alert(err.error?.message || 'Erreur réservation chambre.'); this.loading = false; return throwError(() => err); })
    ).subscribe(res => {
      alert(`Réservé ! Total: ${res.total ?? res.totalAmount} ${res.currency ?? 'TND'}`);
      this.loading = false;
      if (this.dialogRef) this.dialogRef.close('confirmed');
    });
  }
}
