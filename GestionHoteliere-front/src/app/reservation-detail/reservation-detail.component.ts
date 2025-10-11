import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ReservationService } from 'src/app/services/reservation.service';  // Assurez-vous que ce service existe
import { ReservationResponse } from 'src/app/services/reservation.service';

@Component({
  selector: 'app-reservation-detail',
  templateUrl: './reservation-detail.component.html',
  styleUrls: ['./reservation-detail.component.scss']
})
export class ReservationDetailComponent implements OnInit {
  reservation: ReservationResponse | null = null;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private reservationService: ReservationService
  ) {}

  ngOnInit(): void {
    const reservationId = this.route.snapshot.paramMap.get('id');  // Récupérer l'ID de l'URL
    if (reservationId) {
      this.getReservationDetails(reservationId);
    }
  }

  getReservationDetails(id: string): void {
    this.reservationService.getReservationById(Number(id)).subscribe({
      next: (data) => {
        this.reservation = data;
      },
      error: (err) => {
        this.error = 'Erreur de chargement de la réservation';
        console.error(err);
      }
    });
  }
}
