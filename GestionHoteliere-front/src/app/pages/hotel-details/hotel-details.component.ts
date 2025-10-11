import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { map, scan, switchMap, tap } from 'rxjs/operators';
import { BehaviorSubject, Observable } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';

import { HotelService } from 'src/app/services/hotel.service';
import { AuthService } from 'src/app/core/auth.service';
import { Chambre } from 'src/app/models/Chambre';
import { ReservationFormComponent } from 'src/app/reservation/reservation-form/reservation-form.component';

import {
  ReviewService,
  ReviewAggregateDTO,
  ReviewDTO,
  Page
} from 'src/app/services/review.service';

@Component({
  selector: 'app-hotel-details',
  templateUrl: './hotel-details.component.html',
  styleUrls: ['./hotel-details.component.scss']
})
export class HotelDetailsComponent {


  selectedHotelForMap = false;
  hotel$!: Observable<any>;
  chambresDisponibles$!: Observable<Chambre[]>;
  chambreSelectionnee: number | null = null;
  isClient = false;

  readingPeople = Math.floor(Math.random() * 6) + 3;           
  lastBookingMinutes = Math.floor(Math.random() * 120) + 5;     
  lowStock = Math.random() < 0.6;                               
  activeTab: 'overview' | 'services' | 'map' | 'reviews' = 'overview';

  private hotelId!: number;

  agg$!: Observable<ReviewAggregateDTO>;
  ratings$!: Observable<Array<{ name: string; value: number }>>;

  private page$ = new BehaviorSubject<number>(0);
  private pageSize = 6;
  private reset$ = new BehaviorSubject<void>(undefined);

  reviews$!: Observable<ReviewDTO[]>;
  hasMore$ = new BehaviorSubject<boolean>(true);
  loadingReviews$ = new BehaviorSubject<boolean>(false);
   userName = 'guest';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private hotelService: HotelService,
    private authService: AuthService,
    private dialog: MatDialog,
    private reviewService: ReviewService
  ) {}

  ngOnInit(): void {
    this.hotelId = Number(this.route.snapshot.paramMap.get('id'));
    if (this.hotelId) {
      this.hotel$ = this.hotelService.getHotelById(this.hotelId);
      this.chambresDisponibles$ = this.hotelService.getChambresByHotelId(this.hotelId).pipe(
        map((data: any[]) => data.filter(chambre => chambre.dispo === true))
      );
    }

    this.authService.isLoggedIn().then(logged => {
      if (logged) {
        this.isClient = this.authService.hasRole('ROLE_CLIENT');
      }
    });
    this.agg$ = this.reviewService.getAggregates(this.hotelId);

    this.ratings$ = this.agg$.pipe(
      map(a =>
        Object.entries(a.criteriaAverages || {}).map(([name, value]) => ({ name, value }))
      )
    );
    this.reviews$ = this.reset$.pipe(
      switchMap(() =>
        this.page$.pipe(
          tap(() => this.loadingReviews$.next(true)),
          switchMap(page => this.reviewService.getReviews(this.hotelId, page, this.pageSize)),
          tap((p: Page<ReviewDTO>) => {
            this.hasMore$.next(!p.last);
            this.loadingReviews$.next(false);
          }),
          scan((acc: ReviewDTO[], pageData: Page<ReviewDTO>) => {
            return pageData.number === 0 ? pageData.content : acc.concat(pageData.content);
          }, [])
        )
      )
    );
  }


  loadMoreReviews() {
    if (this.hasMore$.value && !this.loadingReviews$.value) {
      this.page$.next(this.page$.value + 1);
    }
  }

  refreshReviews() {
    this.page$.next(0);
    this.reset$.next();
  }

  // --- Base existante (inchangée) ---
  onReservationConfirmee() {
    alert('Réservation enregistrée !');
    this.chambreSelectionnee = null;
  }

  redirectToLogin() {
    this.authService.login();
    console.log("Redirection vers la page de connexion...");
  }

  onServiceReservationConfirmee() {
    alert('Service réservé avec succès !');
  }

  onImgError(event: Event) {
    (event.target as HTMLImageElement).src = 'assets/images/hotels/default.jpg';
  }

  openReservationModal(chambreId: number) {
    this.dialog.open(ReservationFormComponent, {
      width: '600px',
      panelClass: 'custom-reservation-dialog',
      data: { chambreId, type: 'chambre' }
    }).afterClosed().subscribe(result => {
      if (result === 'confirmed') this.onReservationConfirmee();
    });
  }

  openServiceReservationModal(serviceId: number) {
    this.dialog.open(ReservationFormComponent, {
      width: '600px',
      panelClass: 'custom-reservation-dialog',
      data: { type: 'service', serviceId }
    }).afterClosed().subscribe(result => {
      if (result === 'confirmed') this.onServiceReservationConfirmee();
    });
  }

  openMap(hotel: any) {
    this.router.navigate(['/map-overlay'], {
      queryParams: {
        lat: hotel.latitude || hotel.lat,
        lng: hotel.longitude || hotel.lng,
        nom: hotel.nom
      }
    });
  }

  // --- Petites aides UI ---
  setTab(t: 'overview' | 'services' | 'map' | 'reviews') {
    this.activeTab = t;
    if (t === 'map') {
      const target = document.getElementById('section-map');
      if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }

  scrollToChambres() {
    const el = document.getElementById('section-chambres');
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}
