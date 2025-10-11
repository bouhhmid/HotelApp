import { Component, OnInit } from '@angular/core';
import { ReservationService } from 'src/app/services/reservation.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-mes-reservations',
  templateUrl: './mes-reservations.component.html',
  styleUrls: ['./mes-reservations.component.scss']
})
export class MesReservationsComponent implements OnInit {
  reservations: any[] = [];
  page = 0;
  size = 5;
  totalPages = 0;
  selectedStatut: string = '';

  constructor(
    private reservationService: ReservationService,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    this.fetchReservations();
  }

  fetchReservations() {
    this.reservationService.getMesReservations(this.page, this.size, this.selectedStatut).subscribe({
      next: data => {
        this.reservations = data.content;
        this.totalPages = data.totalPages;
      },
      error: err => console.error(err)
    });
  }

  changerPage(direction: number) {
    this.page += direction;
    this.fetchReservations();
  }

  filtrerParStatut(statut: string) {
    this.selectedStatut = statut;
    this.page = 0;
    this.fetchReservations();
  }

  payerReservation(reservationId: number) {
    this.http.post<{ url: string }>(`http://localhost:8081/api/paiement/checkout/${reservationId}`, {})
      .subscribe({
        next: (res) => {
          if (res.url) {
            window.location.href = res.url;  
          }
        },
        error: (err) => {
          console.error("Erreur Stripe : ", err);
          alert("Erreur lors de l'initialisation du paiement.");
        }
      });
  }
  annulerReservation(reservationId: number) {
    if (!confirm('Confirmer l’annulation ?')) return;
    this.reservationService.annuler(reservationId).subscribe({
      next: () => this.fetchReservations(),
      error: (err) => alert(err?.error?.message || 'Erreur lors de l’annulation')
    });
  }

}
