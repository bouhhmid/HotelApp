// src/app/services/hotel.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, throwError } from 'rxjs';
import { environment } from 'src/environments/environment'; // ✅ chemin correct dans Angular

export interface Hotel {
  id: number;
  nom: string;
  adresse: string;
  etoiles: number;
  description: string;
  imageUrl: string;
  latitude: number;
  longitude: number;
  prix:number
}

@Injectable({ providedIn: 'root' })
export class HotelService {
  // ✅ unifie la base à partir de l’environnement
  private readonly apiUrl = `${environment.apiUrl}/hotels`;

  constructor(private http: HttpClient) {}

  getAllHotels(): Observable<Hotel[]> {
    return this.http.get<Hotel[]>(this.apiUrl);
  }

  getHotelById(id: number): Observable<Hotel> {
    return this.http.get<Hotel>(`${this.apiUrl}/${id}`);
  }

  // ✅ garde la même route que ton back pour les chambres (mais base unifiée)
  getChambresByHotelId(hotelId: number): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiUrl}/chambres/hotel/${hotelId}`);
  }

  /** Liste des hôtels de l’admin (ton back: GET /api/hotels/my-hotels) */
 // src/app/services/hotel.service.ts
getMyHotels(): Observable<Hotel[]> {
  return this.http.get<Hotel[]>(`${this.apiUrl}/my-hotels`);
}

getHotelDeAdmin(): Observable<Hotel> {
  return this.getMyHotels().pipe(
    map(list => {
      if (!list || list.length === 0) {
        throw new Error('Aucun hôtel trouvé pour cet administrateur.');
      }
      return list[0];
    })
  );
}

  
}
