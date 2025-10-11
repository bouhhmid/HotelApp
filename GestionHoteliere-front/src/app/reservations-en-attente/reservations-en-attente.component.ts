// src/app/pages/reservations-en-attente/reservations-en-attente.component.ts
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReservationService, ReservationResponse, Page } from 'src/app/services/reservation.service';

@Component({
  selector: 'app-reservations-en-attente',
  templateUrl: './reservations-en-attente.component.html',
  styleUrls: ['./reservations-en-attente.component.scss']
})
export class ReservationsEnAttenteComponent implements OnInit {
  pageIndex = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  rows: ReservationResponse[] = [];
  loading = false;
  error?: string;

  // <- nouvel état
  hotelId?: number | null;

  constructor(
    private reservationService: ReservationService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // lit le ?hotelId=... depuis l’URL (arrive du dashboard)
    this.route.queryParamMap.subscribe(params => {
      const v = params.get('hotelId');
      this.hotelId = v ? +v : null;
      this.pageIndex = 0;
      this.load();
    });
  }

  load(page = this.pageIndex): void {
    this.loading = true;

    const obs = (this.hotelId != null)
      ? this.reservationService.getEnAttenteByHotel(this.hotelId, page, this.pageSize)
      : this.reservationService.getEnAttente(page, this.pageSize); // fallback

    obs.subscribe({
      next: (p: Page<ReservationResponse>) => {
        this.rows = p.content || [];
        this.pageIndex = p.number;
        this.pageSize = p.size;
        this.totalPages = p.totalPages;
        this.totalElements = p.totalElements;
        this.loading = false;
      },
      error: (err) => {
        const status = err?.status;
        this.error =
          status === 403
            ? 'Accès interdit à cet hôtel.'
            : (err?.error?.message || 'Erreur de chargement');
        this.loading = false;
      }
    });
  }

  prev(): void { if (this.pageIndex > 0) this.load(this.pageIndex - 1); }
  next(): void { if (this.pageIndex + 1 < this.totalPages) this.load(this.pageIndex + 1); }

  confirmer(r: ReservationResponse): void {
    if (!confirm(`Confirmer la réservation #${r.id} ?`)) return;
    this.reservationService.confirmer(r.id).subscribe({
      next: () => this.load(),
      error: (err) => alert(err?.error?.message || 'Erreur lors de la confirmation')
    });
  }

  annuler(r: ReservationResponse): void {
    if (!confirm(`Annuler la réservation #${r.id} ?`)) return;
    this.reservationService.annuler(r.id).subscribe({
      next: () => this.load(),
      error: (err) => alert(err?.error?.message || 'Erreur lors de l’annulation')
    });
  }
}
