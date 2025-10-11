// src/app/services/chambre.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';
import { ChambreCreateDTO } from 'src/app/models/Chambre'; 

@Injectable({ providedIn: 'root' })
export class ChambreService {
  private apiUrl = `${environment.apiUrl}/chambres`;

  constructor(private http: HttpClient) {}

  getDateDisponible(chambreId: number): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/${chambreId}/disponibilites`);
  }

  ajouterChambre(hotelId: number, dto: ChambreCreateDTO): Observable<any> {
    return this.http.post(`${this.apiUrl}/ajouter/${hotelId}`, dto);
  }
  markDirty(chambreId: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${chambreId}/dirty`, {});
  }
  markClean(chambreId: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${chambreId}/clean`, {});
  }
}
