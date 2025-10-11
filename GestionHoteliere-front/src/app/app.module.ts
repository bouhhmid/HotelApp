import { NgModule, APP_INITIALIZER } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { FormsModule } from '@angular/forms';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { KeycloakAngularModule, KeycloakService } from 'keycloak-angular';
import { initializeKeycloak } from './keycloak-init.factory';
import { AdminDashboardComponent } from './pages/admin-dashboard/admin-dashboard.component';
import { SuperadminDashboardComponent } from './pages/superadmin-dashboard/superadmin-dashboard.component';
import { ClientHomeComponent } from './pages/client-home/client-home.component';
import { HomeComponent } from './pages/home/home.component';
import { HttpClientModule } from '@angular/common/http';
import { HotelDetailsComponent } from './pages/hotel-details/hotel-details.component';
import { ReservationFormComponent } from './reservation/reservation-form/reservation-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MapOverlayComponent } from './pages/map-overlay/map-overlay.component';
import { LeafletMapComponent } from './shared/leaflet-map/leaflet-map.component';
import { AjouterChambreComponent } from './admin/ajouter-chambre/ajouter-chambre.component';
import { MesReservationsComponent } from './pages/mes-reservations/mes-reservations.component';
import { ChatbotComponent } from './chatbot/chatbot.component';
import { MatSelectModule } from '@angular/material/select';
import { MatDialogModule } from '@angular/material/dialog';
import { ReservationsEnAttenteComponent } from './reservations-en-attente/reservations-en-attente.component';
import { NotificationsComponent } from './notifications/notifications.component';
import { ReservationDetailComponent } from './reservation-detail/reservation-detail.component';
import { ReservationsListComponent } from './reservations-list/reservations-list.component';
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import localeEn from '@angular/common/locales/en';
import localeDe from '@angular/common/locales/de';
import localeIt from '@angular/common/locales/it';
import localeAr from '@angular/common/locales/ar';
import { HousekeepingTodayComponent } from './housekeeping-today/housekeeping-today.component';
import { AiInsightCardComponent } from './ai-insight-card/ai-insight-card.component';
import { SpinWheelComponent } from './spin-wheel/spin-wheel.component';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RoomsBoardComponent } from './rooms-board/rooms-board.component';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
registerLocaleData(localeFr, 'fr');
registerLocaleData(localeEn, 'en');
registerLocaleData(localeDe, 'de');
registerLocaleData(localeIt, 'it');
registerLocaleData(localeAr, 'ar');

@NgModule({
  declarations: [
    AppComponent,
    AdminDashboardComponent,
    SuperadminDashboardComponent,
    ClientHomeComponent,
    HomeComponent,
    HotelDetailsComponent,
    ReservationFormComponent,
    MapOverlayComponent,
    LeafletMapComponent,
    AjouterChambreComponent,
    MesReservationsComponent,
    ChatbotComponent,
    ReservationsEnAttenteComponent,
    NotificationsComponent,
    ReservationDetailComponent,
    ReservationsListComponent,
    HousekeepingTodayComponent,
    AiInsightCardComponent,
    SpinWheelComponent,
    RoomsBoardComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    RouterModule,
    HttpClientModule,
    ReactiveFormsModule,
    BrowserAnimationsModule,
    KeycloakAngularModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatIconModule,
    MatButtonModule,
    BrowserAnimationsModule,
    LeafletModule,
    MatSelectModule,
    BrowserAnimationsModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSnackBarModule
     // Add SwiperModule here
    
    
  ],
  providers: [
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true,
      deps: [KeycloakService]
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
