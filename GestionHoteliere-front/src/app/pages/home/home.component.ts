import { Component, OnInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { HotelService } from 'src/app/services/hotel.service';
import { AuthService } from 'src/app/core/auth.service';
import { ChatbotService } from 'src/app/services/chatbot.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit, OnDestroy {
  // ====== Données page d’accueil ======
  hotels: any[] = [];
  filter = {
    nom: '',
    etoiles: null as number | null,
    adresse: '',
    adultes: 2,
    enfants: 0,
    chambres: 1,
  };
  isLoggedIn: boolean = false;
  userName: string = '';
  isLoading: boolean = true;

  // UI/animations
  showWelcomeAnimation: boolean = true;
  filterAnimationDelay: number = 0;

  // ====== Chatbot: refs DOM (scroll auto) ======
  @ViewChild('messagesContainer') messagesContainer?: ElementRef<HTMLDivElement>;

  constructor(
    private hotelService: HotelService,
    private authService: AuthService,
    private chatbotService: ChatbotService
  ) {}

  // ===================== Cycle de vie =====================
  ngOnInit(): void {
    this.isLoading = true;
    this.initializeAnimations();

    this.hotelService.getAllHotels().subscribe({
      next: (data) => {
        this.hotels = data;
        this.isLoading = false;
        this.animateHotelCards();
      },
      error: (error) => {
        console.error('Erreur lors de la récupération des hôtels :', error);
        this.isLoading = false;
      }
    });

    this.authService.isLoggedIn().then((logged) => {
      this.isLoggedIn = logged;
      if (logged) {
        this.authService.getUserProfile().then((profile) => {
          this.userName = profile.firstName || profile.username || 'Utilisateur';
          this.authService.syncUser();
        });
      }
    });
  }

  ngOnDestroy(): void {
    const ripples = document.querySelectorAll('span[style*="ripple"]');
    ripples.forEach(ripple => ripple.remove());
  }

  // ===================== Animations locales =====================
  private initializeAnimations(): void {
    setTimeout(() => {
      this.showWelcomeAnimation = false;
    }, 1000);
  }

  private animateHotelCards(): void {
    const cards = document.querySelectorAll('.hotel-card');
    cards.forEach((card, index) => {
      (card as HTMLElement).style.animationDelay = `${index * 0.1}s`;
    });
  }

  // ===================== Auth actions =====================
  login() { this.authService.login(); }
  logout() { this.authService.logout(); }

  // ===================== UI helpers =====================
  onImageError(event: Event): void {
    const target = event.target as HTMLImageElement;
    target.src = 'assets/images/default-hotel.jpg';
  }

  onDetailsClick(event: MouseEvent): void {
    event.stopPropagation();
    const button = event.target as HTMLElement;
    this.addRippleEffect(button, event);
  }

  private addRippleEffect(element: HTMLElement, event: MouseEvent): void {
    const rect = element.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    const x = event.clientX - rect.left - size / 2;
    const y = event.clientY - rect.top - size / 2;

    const ripple = document.createElement('span');
    ripple.style.cssText = `
      position: absolute;
      width: ${size}px;
      height: ${size}px;
      left: ${x}px;
      top: ${y}px;
      background: rgba(255, 255, 255, 0.3);
      border-radius: 50%;
      transform: scale(0);
      animation: ripple 0.6s linear;
      pointer-events: none;
    `;
    element.style.position = 'relative';
    element.style.overflow = 'hidden';
    element.appendChild(ripple);
    setTimeout(() => ripple.remove(), 600);
  }

  getBackgroundStyle(imageUrl: string): string {
    if (!imageUrl) return "url('assets/images/hotels/default.jpg')";
    const isExternal = imageUrl.startsWith('http') || imageUrl.startsWith('https');
    return isExternal ? `url('${imageUrl}')` : `url('assets/images/hotels/${imageUrl}')`;
  }

  // ===================== Filtres =====================
  get filteredHotels() {
    const filtered = this.hotels.filter(hotel => {
      const matchNom =
        this.filter.nom === '' ||
        (hotel.nom || '').toLowerCase().includes(this.filter.nom.toLowerCase());

      const hotelStars = hotel?.etoiles != null ? Number(hotel.etoiles) : null;
      const matchEtoiles =
        this.filter.etoiles == null || hotelStars === this.filter.etoiles;

      const addrField = (hotel.ville ?? hotel.adresse ?? '').toLowerCase();
      const matchAdresse =
        this.filter.adresse === '' ||
        addrField.includes(this.filter.adresse.toLowerCase());

      const matchAdultes =
        hotel?.capaciteAdulte == null || hotel.capaciteAdulte >= this.filter.adultes;

      const matchEnfants =
        hotel?.capaciteEnfant == null || hotel.capaciteEnfant >= this.filter.enfants;

      const chambresVal =
        hotel?.capaciteChambre ??
        hotel?.nbChambresDispo ??
        (hotel?.chambres?.length ?? null);

      const matchChambres =
        chambresVal == null || chambresVal >= this.filter.chambres;

      return (
        matchNom && matchEtoiles && matchAdresse &&
        matchAdultes && matchEnfants && matchChambres
      );
    });

    if (filtered.length !== this.hotels.length) {
      setTimeout(() => this.animateHotelCards(), 100);
    }
    return filtered;
  }

  resetFilters() {
    this.filter = { nom: '', etoiles: null, adresse: '', adultes: 2, enfants: 0, chambres: 1 };
    const filterInputs = document.querySelectorAll('.filters input, .filters select');
    filterInputs.forEach((input, index) => {
      setTimeout(() => {
        (input as HTMLElement).classList.add('filter-reset-animation');
        setTimeout(() => (input as HTMLElement).classList.remove('filter-reset-animation'), 300);
      }, index * 50);
    });
  }

  getStarsArray(count: number): number[] {
    return Array(count).fill(0).map((_, i) => i);
  }

  isPremiumHotel(hotel: any): boolean {
    const stars = hotel?.etoiles != null ? Number(hotel.etoiles) : 0;
    return stars === 5;
  }

  trackByHotelId = (_: number, h: any) => h.id;

  scrollToFilters(): void {
    const filtersElement = document.querySelector('.filters');
    if (filtersElement) filtersElement.scrollIntoView({ behavior: 'smooth' });
  }

  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  private isMobile(): boolean {
    return window.innerWidth <= 768;
  }

  private shouldReduceAnimations(): boolean {
    return this.isMobile() || window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  }

  private initializePerformanceOptimizations(): void {
    if (this.shouldReduceAnimations()) document.body.classList.add('reduce-animations');

    if ('IntersectionObserver' in window) {
      const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => { if (entry.isIntersecting) entry.target.classList.add('animate-in'); });
      }, { threshold: 0.1 });

      setTimeout(() => {
        const cards = document.querySelectorAll('.hotel-card');
        cards.forEach(card => observer.observe(card));
      }, 500);
    }
  }

  // ===================== Chatbot: état =====================
  isOpen = false;
  hasUnreadMessages = false;
  isTyping = false;
  currentMessage = '';

  messages: Array<{ id: string; text: string; isUser: boolean }> = [];
  trackByMessageId = (_: number, m: { id: string }) => m.id;

  // ===================== Chatbot: actions =====================
  toggleChat() {
    this.isOpen = !this.isOpen;
    if (this.isOpen) this.hasUnreadMessages = false;
    this.scrollToBottomSoon();
  }

  closeChat() { this.isOpen = false; }

  sendQuickMessage(text: string) {
    this._pushUser(text);
    this._askBot(text); // appel réel
  }

  sendMessage() {
    const text = this.currentMessage.trim();
    if (!text) return;
    this._pushUser(text);
    this.currentMessage = '';
    this._askBot(text); // appel réel
  }

  // Typage strict ici ✅ (HTML: (keydown.enter)="onEnterPressed($event as KeyboardEvent)")
  onEnterPressed(ev: KeyboardEvent) {
    if (!ev.shiftKey) {
      ev.preventDefault();
      this.sendMessage();
    }
  }

  onInputChange() {
    // (optionnel) auto-resize du textarea
  }

  // ===================== Chatbot: helpers =====================
  private _pushUser(text: string) {
    const id = (globalThis as any).crypto?.randomUUID?.() ?? `${Date.now()}-u`;
    this.messages.push({ id, text, isUser: true });
    this.scrollToBottomSoon();
  }

  private _pushBot(text: string) {
    const id = (globalThis as any).crypto?.randomUUID?.() ?? `${Date.now()}-b`;
    this.messages.push({ id, text, isUser: false });
    this.scrollToBottomSoon();
  }

  // --- Appel réel vers ton backend/Ollama ---
  private _askBot(userText: string) {
    this.isTyping = true;
    this.chatbotService.envoyerMessage(userText).subscribe({
      next: (replyText: string) => {
        const reply = (replyText ?? '').toString().trim() || "⚠️ Réponse vide reçue du chatbot.";
        this._pushBot(reply);
        this.isTyping = false;
        if (!this.isOpen) this.hasUnreadMessages = true;
      },
      error: (err) => {
        console.error('Erreur chatbot :', err);
        this._pushBot("⚠️ Désolé, le service chatbot est indisponible pour le moment.");
        this.isTyping = false;
      }
    });
  }

  private scrollToBottomSoon() {
    setTimeout(() => this.scrollToBottom(), 0);
  }

  private scrollToBottom() {
    if (!this.messagesContainer) return;
    const el = this.messagesContainer.nativeElement;
    el.scrollTop = el.scrollHeight;
  }
}
