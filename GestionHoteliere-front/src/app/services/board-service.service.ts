import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';
import { RoomBoardDTO } from 'src/app/models/board';
@Injectable({
  providedIn: 'root'
})
export class BoardServiceService {
  private api = environment.apiUrl; 
  constructor(private http: HttpClient) {}

  getBoard(hotelId: number): Observable<RoomBoardDTO[]> {
    return this.http.get<RoomBoardDTO[]>(`${this.api}/hotels/${hotelId}/rooms/board`);
  }
  markDirty(chambreId: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/chambres/${chambreId}/dirty`, {});
  }
  markClean(chambreId: number): Observable<void> {
    return this.http.patch<void>(`${this.api}/chambres/${chambreId}/clean`, {});
  }
}
