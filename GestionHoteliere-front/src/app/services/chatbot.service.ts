// src/app/services/chatbot.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChatbotService {
  private apiUrl = 'http://localhost:8081/api/chat';

  constructor(private http: HttpClient) {}

  envoyerMessage(message: string): Observable<any> {
    // Important : on force la réponse texte si backend renvoie un string
    return this.http.post(this.apiUrl, { message }, { responseType: 'text' });
  }
}
