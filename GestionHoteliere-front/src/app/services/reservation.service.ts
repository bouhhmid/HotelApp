// src/app/services/reservation.service.ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

/** --- MODELS --- */
export type Statut = 'EN_ATTENTE' | 'CONFIRMEE' | 'ANNULEE';

export interface ReservationChambreRequest {
  chambreId: number;
  dateDebut: string;   // "YYYY-MM-DDTHH:mm:ss"
  dateFin: string;     // "YYYY-MM-DDTHH:mm:ss"
  nbAdultes: number;
  nbEnfants?: number;
  nbChambres: number;
  currency?: string;
}

export interface ReservationServiceRequest {
  serviceHotelId: number;
  dateTime: string;    // "YYYY-MM-DDTHH:mm:ss"
  participants: number;
  currency?: string;
}

export interface ReservationResponse {
  id: number;
  statut: Statut;
  dateReservation: string;
  dateDebut?: string;
  dateFin?: string;
  dateTime?: string;
  total?: number;        // pour createChambreOption
  totalAmount?: number;  // pour createService / reserverServiceV2
  currency?: string;
  nbAdultes?: number;
  nbEnfants?: number;
  nbChambres?: number;
  participants?: number;
  chambreId?: number | null;
  serviceId?: number | null;
}

export interface ChambreOption {
  id: number;
  chambreId: number;
  typeChambre: string;
  vue: 'MER' | 'PISCINE' | 'JARDIN' | 'VILLE' | string;
  prixParNuitParPersonne: number;
}

export interface ReservationChambreOptionRequestDTO {
  optionId: number;
  dateDebut: string;
  dateFin: string;
  nbAdultes: number;
  nbEnfants?: number;
  nbChambres?: number;
}

export interface ReservationChambreQuoteDTO {
  nuits: number;
  capaciteRequise: number;
  capaciteMax: number;
  capacityOk: boolean;
  subtotal: number;
  taxes: number;
  frais: number;
  total: number;
  currency: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // index de page (0-based)
  size: number;
}

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private readonly api = environment.apiUrl;                 // ex: http://localhost:8081/api
  private readonly url = `${this.api}/reservations`;

  constructor(private http: HttpClient) { }

  /** ===== CHAMBRE (classique) ===== */
  quoteChambre(dto: ReservationChambreRequest): Observable<ReservationChambreQuoteDTO> {
    return this.http.post<ReservationChambreQuoteDTO>(`${this.url}/chambres/quote`, dto);
  }

  createChambre(dto: ReservationChambreRequest): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(`${this.url}/chambres`, dto);
  }

  getDisponibilites(chambreId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.api}/chambres/${chambreId}/disponibilites`);
  }

  createService(dto: ReservationServiceRequest): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(`${this.url}/services`, dto);
  }

  reserverService(serviceId: number, dateDebut: string, dateFin: string): Observable<any> {
    const params = new HttpParams().set('dateDebut', dateDebut).set('dateFin', dateFin);
    return this.http.post(`${this.url}/service/${serviceId}`, null, { params });
  }

  getOptionsByChambre(chambreId: number): Observable<ChambreOption[]> {
    return this.http.get<ChambreOption[]>(`${this.api}/chambres/${chambreId}/options`);
  }

  quoteChambreOption(body: ReservationChambreOptionRequestDTO): Observable<ReservationChambreQuoteDTO> {
    return this.http.post<ReservationChambreQuoteDTO>(`${this.url}/chambres/options/quote`, body);
  }

  createChambreOption(body: ReservationChambreOptionRequestDTO): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(`${this.url}/chambres/options`, body);
  }

  /** ===== MES RÉSERVATIONS (client) ===== */
  getMesReservations(page = 0, size = 10, statut?: string): Observable<Page<ReservationResponse>> {
    let params = `?page=${page}&size=${size}`;
    const s = statut?.trim();
    if (s) params += `&statut=${encodeURIComponent(s)}`;
    return this.http.get<Page<ReservationResponse>>(`${this.url}/me${params}`);
  }

  getEnAttente(page = 0, size = 10): Observable<Page<ReservationResponse>> {
    return this.http.get<Page<ReservationResponse>>(`${this.url}/en-attente?page=${page}&size=${size}`);
  }

  getPendingCount(): Observable<number> {
    return this.http
      .get<Page<ReservationResponse>>(`${this.url}/en-attente?page=0&size=1`)
      .pipe(map(p => p?.totalElements ?? 0));
  }

  confirmer(reservationId: number): Observable<ReservationResponse> {
    return this.http.put<ReservationResponse>(`${this.url}/${reservationId}/confirmer`, {});
  }

  annuler(reservationId: number): Observable<void> {
    return this.http.put<void>(`${this.url}/${reservationId}/annuler`, {});
  }
  getByHotel(hotelId: number, page = 0, size = 10, statut?: Statut) {
    let url = `${this.url}/hotel/${hotelId}?page=${page}&size=${size}`;
    if (statut) url += `&statut=${encodeURIComponent(statut)}`;
    return this.http.get<Page<ReservationResponse>>(url);
  }

  getEnAttenteByHotel(hotelId: number, page = 0, size = 10) {
    return this.http.get<Page<ReservationResponse>>(
      `${this.url}/en-attente/hotel/${hotelId}?page=${page}&size=${size}`
    );
  }
  getReservationById(id: number): Observable<ReservationResponse> {
    return this.http.get<ReservationResponse>(`${this.api}/reservations/${id}`);
  }

getCalendar(chambreId: number, from?: string, to?: string, tz = 'Africa/Tunis'): Observable<string[]> {
  let params = new HttpParams().set('tz', tz);
  if (from) params = params.set('from', from);
  if (to)   params = params.set('to', to);
  return this.http.get<string[]>(`${this.api}/reservations/chambres/${chambreId}/calendar`, { params });
}
}
