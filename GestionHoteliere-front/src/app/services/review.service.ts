import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
// --- Models (si tu as déjà un dossier models, tu peux déplacer ces interfaces) ---
export interface ReviewDTO {
  id: number;
  userName: string;
  score: number;
  title: string;
  comment: string;
  createdAt: string; // ISO
}

export interface ReviewDTOIn {
  score: number;
  title: string;
  comment: string;
  proprete?: number;
  emplacement?: number;
  confort?: number;
  qualitePrix?: number;
  wifi?: number;
}

export interface ReviewAggregateDTO {
  average: number;
  count: number;
  criteriaAverages: Record<string, number>; // { 'Propreté': 8.9, ... }
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number; // page index
  last: boolean;
}

@Injectable({ providedIn: 'root' })
export class ReviewService {
  private base = `${environment.apiUrl}/hotels`;

  constructor(private http: HttpClient) {}

  getAggregates(hotelId: number): Observable<ReviewAggregateDTO> {
    return this.http.get<ReviewAggregateDTO>(`${this.base}/${hotelId}/reviews/aggregates`);
  }

  getReviews(hotelId: number, page = 0, size = 10): Observable<Page<ReviewDTO>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<ReviewDTO>>(`${this.base}/${hotelId}/reviews`, { params });
  }

  addReview(hotelId: number, body: ReviewDTOIn): Observable<ReviewDTO> {
    return this.http.post<ReviewDTO>(`${this.base}/${hotelId}/reviews`, body);
  }
}

