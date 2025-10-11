import { Component, Input, OnInit } from '@angular/core';

type Prize = { label: string; icon: string; weight: number; code?: string; isLose?: boolean };

@Component({
  selector: 'app-spin-wheel',
  templateUrl: './spin-wheel.component.html',
  styleUrls: ['./spin-wheel.component.scss']
})
export class SpinWheelComponent implements OnInit {
  /** Passe ces inputs depuis ta page (ex: [hotelId]="hotel.id" [userId]="userName") */
  @Input() hotelId!: number;
  @Input() userId!: string;

  spinning = false;
  rotation = 0;
  result?: Prize;
  message = '';
  muted = false;

  /** 8 secteurs = 360/8 = 45° */
  prizes: Prize[] = [
    { label: '🎁 Boisson de bienvenue', icon: '🥤', weight: 18, code: 'WELCOME_DRINK' },
    { label: '🏋️ Accès salle de sport', icon: '🏋️', weight: 16, code: 'GYM_PASS' },
    { label: '💆 -10% Spa',              icon: '💆', weight: 14, code: 'SPA_10' },
    { label: '🍽️ Dessert offert',       icon: '🍰', weight: 12, code: 'FREE_DESSERT' },
    { label: '🕒 Late checkout',         icon: '🕒', weight: 10, code: 'LATE_CHECKOUT' },
    { label: '🧃 Jus gratuit',           icon: '🧃', weight: 14, code: 'JUICE' },
    { label: '🧖 Sauna 30 min',          icon: '🧖', weight: 10, code: 'SAUNA_30' },
    { label: '🙂 Réessaie demain',       icon: '🙂', weight: 6,  code: 'TRY_AGAIN', isLose: true },
  ];

  /** Sons */
  private spinSfx  = new Audio('assets/sounds/spin.mp3');  // court, en boucle pendant la rotation
  private winSfx   = new Audio('assets/sounds/win.mp3');   // jingle gain
  private loseSfx  = new Audio('assets/sounds/lose.mp3');  // jingle perte (à ajouter)

  ngOnInit(): void {
    this.muted = localStorage.getItem('wheel:muted') === '1';

    // Config audio
    this.spinSfx.loop = true;
    this.spinSfx.volume = 0.35;
    this.winSfx.volume  = 0.6;
    this.loseSfx.volume = 0.6;
  }

  /** 1 tirage / jour maxi (clé par hôtel + utilisateur) */
  get canSpin(): boolean {
    const key = this.keyLastSpin();
    const last = localStorage.getItem(key);
    if (!last) return true;
    return new Date(last).toDateString() !== new Date().toDateString();
  }

  /** Couleurs des secteurs (tu peux éditer la palette) */
  getSectorColor(i: number): string {
    const colors = ['#fde68a','#bfdbfe','#c4b5fd','#a7f3d0','#fecaca','#fcd34d','#93c5fd','#ddd6fe'];
    return colors[i % colors.length];
  }

  toggleMute(): void {
    this.muted = !this.muted;
    localStorage.setItem('wheel:muted', this.muted ? '1' : '0');
    if (this.muted) {
      this.safeStop(this.spinSfx);
      this.safeStop(this.winSfx);
      this.safeStop(this.loseSfx);
    }
  }

  async spin(): Promise<void> {
    if (this.spinning || !this.canSpin) return;

    this.spinning = true;
    this.result = undefined;
    this.message = '';

    // Target via poids
    const targetIndex = this.weightedPickIndex(this.prizes.map(p => p.weight));
    const n = this.prizes.length;
    const sectorAngle = 360 / n;

    // On vise le centre approximatif du secteur choisi, avec un petit aléa
    const randomNudge = Math.random() * 10 - 5;
    const targetAngleFromTop = (targetIndex * sectorAngle) + (sectorAngle / 2) + randomNudge;

    // Nombre de tours complet + alignement final sur l’aiguille
    const fullTurns = 6;
    const finalRotation = (fullTurns * 360) + (360 - targetAngleFromTop);

    // ▶️ son de spin
    if (!this.muted) {
      this.safeRewind(this.spinSfx);
      await this.safePlay(this.spinSfx);
    }

    // Lance l’animation CSS (voir .wheel { transition: 3.2s … })
    this.rotation = (this.rotation % 360) + finalRotation;

    // Fin d’animation au bout de ~3.2s
    setTimeout(async () => {
      this.spinning = false;
      this.result = this.prizes[targetIndex];

      // stop spin
      this.safeStop(this.spinSfx);

      // Sons finaux
      if (!this.muted) {
        if (this.result.isLose) {
          this.safeRewind(this.loseSfx);
          await this.safePlay(this.loseSfx);
        } else {
          this.safeRewind(this.winSfx);
          await this.safePlay(this.winSfx);
        }
      }

      // Marque la date du dernier tirage
      localStorage.setItem(this.keyLastSpin(), new Date().toISOString());

      // Message
      this.message = this.result.isLose
        ? 'Pas de chance… retente demain !'
        : 'Bravo ! Va à la réception pour profiter de ton avantage.';
    }, 3200);
  }

  // ---- Helpers ----
  private keyLastSpin(): string {
    return `wheel:lastSpin:${this.hotelId}:${this.userId}`;
  }

  private weightedPickIndex(weights: number[]): number {
    const total = weights.reduce((a, b) => a + b, 0);
    let r = Math.random() * total;
    for (let i = 0; i < weights.length; i++) {
      if ((r -= weights[i]) < 0) return i;
    }
    return weights.length - 1;
  }

  private async safePlay(a: HTMLAudioElement) {
    try { await a.play(); } catch { /* ignore autoplay errors */ }
  }
  private safeStop(a: HTMLAudioElement) {
    try { a.pause(); } catch { /* ignore */ }
  }
  private safeRewind(a: HTMLAudioElement) {
    try { a.currentTime = 0; } catch { /* ignore */ }
  }
}
