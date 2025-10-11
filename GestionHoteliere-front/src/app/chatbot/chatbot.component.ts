// src/app/components/chatbot/chatbot.component.ts
import { Component } from '@angular/core';
import { ChatbotService } from 'src/app/services/chatbot.service';

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.scss']
})
export class ChatbotComponent {
  userMessage = '';
  messages: { text: string, sender: 'user' | 'bot' }[] = [];

  constructor(private chatbotService: ChatbotService) {}

  envoyer() {
    const message = this.userMessage.trim();
    if (!message) return;

    this.messages.push({ text: message, sender: 'user' });

    this.chatbotService.envoyerMessage(message).subscribe({
      next: (res) => {
        const parsed = this.extraireTexteDepuisOllama(res);
        this.messages.push({ text: parsed, sender: 'bot' });
      },
      error: () => {
        this.messages.push({ text: 'Erreur de connexion au chatbot.', sender: 'bot' });
      }
    });

    this.userMessage = '';
  }

  extraireTexteDepuisOllama(res: any): string {
    try {
      return typeof res === 'string' ? res : JSON.stringify(res);
    } catch {
      return 'Réponse mal formée.';
    }
  }
  formaterMessage(msg: string): string {
  return msg.replace(/\n/g, '<br>');
}

}
