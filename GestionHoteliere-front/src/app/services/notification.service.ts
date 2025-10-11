import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, map, of } from 'rxjs';
import { environment } from 'src/environments/environment';

export interface Notification {
  id: number;
  titre: string;
  message: string;
  dateEnvoi: string; // ISO
  lu: boolean;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private readonly api = `${environment.apiUrl}/notifications`;
  constructor(private http: HttpClient) {}

  /** Compteur non lus */
  getUnreadCount() {
    return this.http.get<number>(`${this.api}/unread`).pipe(catchError(() => of(0)));
  }

  /** Liste paginée */
  page(page = 0, size = 10) {
    return this.http.get<Page<Notification>>(
      `${this.api}/page?page=${page}&size=${size}`
    );
  }

  /** Pratique pour le dropdown: récupère juste les N dernières */
  fetchLatest(limit = 10) {
    // si ton back n’a pas /latest, on se base sur la page 0
    return this.page(0, limit).pipe(
      map(p => p?.content ?? []),
      catchError(() => of<Notification[]>([]))
    );
  }

  /** Marquer une notif comme lue */
  markAsRead(id: number) {
    return this.http.put<void>(`${this.api}/${id}/lu`, {});
  }

  /** Tout marquer comme lu (si l’endpoint n’existe pas, côté composant on fera un fallback) */
  markAllAsRead() {
    return this.http.put<void>(`${this.api}/lu-all`, {}).pipe(catchError(() => of(void 0)));
  }
}
