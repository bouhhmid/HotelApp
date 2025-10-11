import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ReservationService } from 'src/app/services/reservation.service';

@Component({
  selector: 'app-reservations-list',
  templateUrl: './reservations-list.component.html',
  styleUrls: ['./reservations-list.component.scss']
})
export class ReservationsListComponent implements OnInit {
  reservations: any[] = [];

  constructor(
    private reservationService: ReservationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadReservations();
  }

  loadReservations() {
    // Charger les réservations en attente
    this.reservationService.getEnAttente(0, 10).subscribe(
      (data) => {
        this.reservations = data.content;
      },
      (error) => {
        console.error('Erreur lors du chargement des réservations', error);
      }
    );
  }

  goToDetail(reservationId: number): void {
    this.router.navigate(['/reservation', reservationId]);
  }
}
