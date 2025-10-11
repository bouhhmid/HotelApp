import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

export interface TimePoint { date: string; value: number; }

export interface AdminOverviewDTO {
  reservations7J?: { x: string; y: number }[]; 
  series?: TimePoint[];                         
  windowDays?: number;
  seriesByCheckin?: boolean;
}
export interface AiInsightsDTO {
  insight: string;
  type: string;
  generatedAt: string;
}

export interface DashboardSummaryDTO {
  hotelId: number;
  hotelName?: string | null;
  rangeStart: string;
  rangeEnd: string;
  timezone?: string;
  generatedAt?: string;

  roomsCount: number;
  occupiedRoomsToday: number;
  occupancyRateToday: number;

  pendingCount: number;
  checkinsToday: number;
  checkoutsToday: number;

  nightsSoldInRange: number;
  revenueInRange: number;
}

@Injectable({ providedIn: 'root' })
export class AdminStatsService {
  private readonly base = 'http://localhost:8081/api/admin/stats';
private readonly api = environment.apiUrl; 
  constructor(private http: HttpClient) {}

  /** Overview (séries) */
  getOverview(params: {
    hotelId: number; seriesBy?: 'reservation'|'checkin'; windowDays?: number;
    round?: number; tz?: string; distinct?: boolean; includeTypes?: boolean;
  }): Observable<AdminOverviewDTO> {
    let p = new HttpParams().set('hotelId', String(params.hotelId));
    p = p.set('seriesBy', params.seriesBy ?? 'reservation');
    p = p.set('windowDays', String(params.windowDays ?? 7));
    p = p.set('round', String(params.round ?? 1));
    p = p.set('distinct', String(params.distinct ?? true));
    p = p.set('includeTypes', String(params.includeTypes ?? true));
    if (params.tz) p = p.set('tz', params.tz);
    return this.http.get<AdminOverviewDTO>(`${this.base}/overview`, { params: p });
  }

  /** Résumé compact pour les cartes KPI */
  getSummary(hotelId: number, tz?: string): Observable<DashboardSummaryDTO> {
    let p = new HttpParams().set('hotelId', String(hotelId));
    if (tz) p = p.set('tz', tz);
    return this.http.get<DashboardSummaryDTO>(`${this.base}/summary`, { params: p });
  }

    getAiInsights(hotelId: number, windowDays = 7, tz = 'Africa/Tunis') {
    const params = new HttpParams()
      .set('hotelId', hotelId)
      .set('windowDays', windowDays)
      .set('tz', tz);
    return this.http.get<AiInsightsDTO>(`${this.api}/admin/insights`, { params });
  }
}
