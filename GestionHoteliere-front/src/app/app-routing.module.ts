import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { SuperadminDashboardComponent } from './pages/superadmin-dashboard/superadmin-dashboard.component';
import { ClientHomeComponent } from './pages/client-home/client-home.component';
import { HomeComponent } from './pages/home/home.component';
import { HotelDetailsComponent } from './pages/hotel-details/hotel-details.component';
import { MapOverlayComponent } from './pages/map-overlay/map-overlay.component';
import { AjouterChambreComponent } from './admin/ajouter-chambre/ajouter-chambre.component';
import { MesReservationsComponent } from './pages/mes-reservations/mes-reservations.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { ReservationsEnAttenteComponent } from './reservations-en-attente/reservations-en-attente.component';
import { ReservationDetailComponent } from './reservation-detail/reservation-detail.component';
import { ReservationsListComponent } from './reservations-list/reservations-list.component';
import { HousekeepingTodayComponent } from './housekeeping-today/housekeeping-today.component';
import { RoomsBoardComponent } from './rooms-board/rooms-board.component';
import { AiInsightCardComponent } from './ai-insight-card/ai-insight-card.component';
const routes: Routes = [
{path: '', redirectTo: 'home', pathMatch: 'full'},
  {
    path: 'admin',
    children: [
      { path: 'dashboard', component: AdminDashboardComponent },
      { path: 'notifications', component: NotificationsComponent },
      { path: 'ajouter-chambre', component: AjouterChambreComponent },
      { path: 'reservations-en-attente', component: ReservationsEnAttenteComponent },
        {path: 'housekeeping-today', component: HousekeepingTodayComponent},
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
  { path: 'superadmin/dashboard', component: SuperadminDashboardComponent },
  { path: 'client/home', component: ClientHomeComponent },
  { path: 'home', component: HomeComponent },
  { path: 'hotel/:id', component: HotelDetailsComponent },
  { path: 'map-overlay', component: MapOverlayComponent },
  { path: 'mes-reservations', component: MesReservationsComponent },
  {path: 'reservation/:id',component: ReservationDetailComponent},
  {path: 'reservations-en-attente',component: ReservationsListComponent },
   {path: 'rooms-board', component: RoomsBoardComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule { }
