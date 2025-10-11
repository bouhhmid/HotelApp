import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';


export interface HousekeepingTask {
  room: string;
  type: 'ARRIVAL'|'DEPARTURE'|'STAYOVER'|string;
  priority: number;     
  dueTime: string;      
}

@Injectable({ providedIn: 'root' })
export class HousekeepingService {
  constructor(private http: HttpClient) {}
  
  private readonly api = environment.apiUrl;        // ex: http://localhost:8081/api
  private readonly url = `${this.api}/housekeeping`;

  getToday(hotelId: number, tz = 'Africa/Tunis') {
    const params = new HttpParams().set('hotelId', hotelId).set('tz', tz);
    return this.http.get<HousekeepingTask[]>(`${this.url}/today`, { params });
  }
}

