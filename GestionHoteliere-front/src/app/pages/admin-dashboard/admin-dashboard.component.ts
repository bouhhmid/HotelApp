import { Component, OnInit, OnDestroy, HostListener, ViewChild, ElementRef } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
import { HotelService } from 'src/app/services/hotel.service';
import { ReservationService } from 'src/app/services/reservation.service';
import { NotificationsService, Notification as Notif } from 'src/app/services/notification.service';
import { Router } from '@angular/router';
import { AdminStatsService, AdminOverviewDTO, DashboardSummaryDTO } from 'src/app/services/admin-stats.service';
import { HousekeepingService, HousekeepingTask } from 'src/app/services/housekeeping.service';
import { Chart, ChartConfiguration } from 'chart.js';
import { Subject, forkJoin, of } from 'rxjs';
import { takeUntil, catchError } from 'rxjs/operators';

type SeriesPoint = { date: string; value: number };

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit, OnDestroy {
  @ViewChild('notifDD', { read: ElementRef }) notifDD!: ElementRef;
  @ViewChild('langDD',  { read: ElementRef }) langDD!: ElementRef;

  private destroy$ = new Subject<void>();

  confirmedToday = 0;

  loading = true;
  error = '';
  displayName = '';
  avatarUrl = '';

  // KPIs / données
  summary?: DashboardSummaryDTO | null;
  overview?: { series: SeriesPoint[]; windowDays: number; seriesByCheckin?: boolean } | null;

  // Divers état UI
  pendingCount = 0;
  unreadCount = 0;
  roomCount = 0;

  hotels: any[] = [];
  selectedHotelId?: number;
  latestPending: any[] = [];

  chart?: Chart;
  readonly tz = 'Africa/Tunis';

  // Notifications
  notifOpen = false;
  notifications: Notif[] = [];
  trackNotif = (_: number, n: Notif) => n.id;

  // Langues
  langOpen = false;
  languages = [
    { code: 'fr', label: 'Français', flag: 'assets/flags/FR.png' },
    { code: 'en', label: 'English',  flag: 'assets/flags/GB.png' },
    { code: 'de', label: 'Deutsch',  flag: 'assets/flags/DE.png' },
    { code: 'it', label: 'Italiano', flag: 'assets/flags/IT.png' },
    { code: 'ar', label: 'العربية',  flag: 'assets/flags/SA.png' },
  ];
  currentLang = this.languages[0];

  // Housekeeping (Today)
  tasksToday: HousekeepingTask[] = [];
  hkCounts = { arrivals: 0, departures: 0, stayovers: 0 };

  constructor(
    private keycloak: KeycloakService,
    private hotelService: HotelService,
    private reservationService: ReservationService,
    private notifService: NotificationsService,
    private statsService: AdminStatsService,
    private housekeepingService: HousekeepingService,
    private router: Router
  ) {}

  async ngOnInit() {
    try {
      this.bindUserFromToken();
      await this.loadHotelsAndData();
      this.refreshUnreadCount();

      const saved = localStorage.getItem('app.lang');
      if (saved) {
        const found = this.languages.find(l => l.code === saved);
        if (found) {
          this.currentLang = found;
          document.dir = (saved === 'ar') ? 'rtl' : 'ltr';
        }
      }
    } catch (e: unknown) {
      this.error = 'Impossible de charger vos hôtels.';
      console.error(e);
    } finally {
      this.loading = false;
    }
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
    this.chart?.destroy();
  }

  /* ===== Dropdowns ===== */
  @HostListener('document:click', ['$event'])
  onDocClick(ev: MouseEvent): void {
    const target = ev.target as Node;
    const clickInsideNotif = this.notifDD?.nativeElement?.contains(target);
    const clickInsideLang  = this.langDD?.nativeElement?.contains(target);
    if (!clickInsideNotif) this.notifOpen = false;
    if (!clickInsideLang)  this.langOpen  = false;
  }

  toggleNotif(): void {
    this.langOpen = false;
    this.notifOpen = !this.notifOpen;
    if (this.notifOpen) this.loadNotifications();
  }
  toggleLang(): void {
    this.notifOpen = false;
    this.langOpen = !this.langOpen;
  }
  setLang(code: string): void {
    const found = this.languages.find(l => l.code === code);
    if (!found) return;
    this.currentLang = found;
    document.dir = (code === 'ar') ? 'rtl' : 'ltr';
    localStorage.setItem('app.lang', code);
    this.langOpen = false;
  }

  /* ===== Notifications (HTTP) ===== */
  private loadNotifications(): void {
    this.notifService.fetchLatest(10)
      .pipe(takeUntil(this.destroy$))
      .subscribe(list => this.notifications = list || []);
  }
  private refreshUnreadCount(): void {
    this.notifService.getUnreadCount()
      .pipe(takeUntil(this.destroy$))
      .subscribe(n => this.unreadCount = n ?? 0);
  }
  openNotification(n: Notif): void {
    if (!n.lu) {
      n.lu = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
      this.notifService.markAsRead(n.id).pipe(takeUntil(this.destroy$)).subscribe();
    }
    this.notifOpen = false;
  }
  markAllAsRead(): void {
    const toMark = this.notifications.filter(x => !x.lu);
    if (!toMark.length) return;
    this.notifications = this.notifications.map(x => ({ ...x, lu: true }));
    this.unreadCount = 0;

    this.notifService.markAllAsRead()
      .pipe(
        takeUntil(this.destroy$),
        catchError(() =>
          forkJoin(toMark.map(n => this.notifService.markAsRead(n.id))).pipe(catchError(() => of(void 0)))
        )
      ).subscribe();
  }
  goToAll(): void {
    this.notifOpen = false;
    this.router.navigate(['/admin/notifications'], { queryParams: { hotelId: this.selectedHotelId } });
  }

  /* ===== Données & Stats ===== */
  private bindUserFromToken() {
    const kp: any = this.keycloak.getKeycloakInstance().tokenParsed || {};
    const name =
      [kp?.given_name, kp?.family_name].filter(Boolean).join(' ').trim()
      || kp?.name || kp?.preferred_username || kp?.email || 'Administrateur';
    this.displayName = name;
    this.avatarUrl = kp?.picture
      ? kp.picture
      : `https://ui-avatars.com/api/?name=${encodeURIComponent(this.displayName)}&background=0D8ABC&color=fff`;
  }

  private async loadHotelsAndData() {
    const hotels = await this.hotelService.getMyHotels().toPromise();
    this.hotels = hotels || [];
    if (this.hotels.length) {
      this.selectedHotelId = this.hotels[0].id;
      this.onHotelChange();
    }
  }

  onHotelChange() {
    this.chart?.destroy();
    this.overview = null;
    this.summary = null;

    if (!this.selectedHotelId) {
      this.roomCount = 0; this.pendingCount = 0; this.latestPending = [];
      this.tasksToday = []; this.hkCounts = { arrivals: 0, departures: 0, stayovers: 0 };
      return;
    }

    // 1) Résumé KPI
    this.statsService.getSummary(this.selectedHotelId!, this.tz)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (s: DashboardSummaryDTO) => {
          this.summary = s;
          this.roomCount = s.roomsCount ?? this.roomCount;
          this.pendingCount = s.pendingCount ?? this.pendingCount;
        },
        error: (err: unknown) => { console.error(err); this.summary = null; }
      });

    // 2) Legacy pour liste en attente + nombre de chambres
    this.hotelService.getChambresByHotelId(this.selectedHotelId).pipe(takeUntil(this.destroy$)).subscribe({
      next: rooms => this.roomCount = rooms?.length || this.roomCount,
      error: (err: unknown) => console.error(err)
    });
    this.reservationService.getEnAttenteByHotel(this.selectedHotelId, 0, 5).pipe(takeUntil(this.destroy$)).subscribe({
      next: p => { this.latestPending = p.content || []; this.pendingCount = p.totalElements ?? this.pendingCount; },
      error: (err: unknown) => console.error(err)
    });

    // 3) Séries pour graphe
    this.reloadOverview();

    // 4) Housekeeping aujourd'hui
    this.loadHousekeepingToday();
  }

  private reloadOverview() {
    if (!this.selectedHotelId) return;

    this.statsService.getOverview({
      hotelId: this.selectedHotelId!,
      seriesBy: 'reservation',
      windowDays: 7,
      round: 1,
      tz: this.tz,
      distinct: true,
      includeTypes: true
    })
    .pipe(takeUntil(this.destroy$))
    .subscribe({
      next: (ov: AdminOverviewDTO) => {
        const series: SeriesPoint[] =
          (ov?.series
          ?? (ov as any)?.reservations7J?.map((p: any) => ({ date: p.x, value: p.y }))) || [];

        const safe = { series, windowDays: ov?.windowDays ?? 7, seriesByCheckin: ov?.seriesByCheckin ?? false };
        this.overview = safe;
        this.renderSeriesChart(safe);
      },
      error: (err: unknown) => { console.error(err); this.overview = null; }
    });
  }

  private renderSeriesChart(
    ov?: { series: SeriesPoint[]; windowDays: number; seriesByCheckin?: boolean } | null
  ) {
    const points = ov?.series ?? [];
    const labels = points.map(p => p.date);
    const data = points.map(p => p.value);

    const cfg: ChartConfiguration<'line'> = {
      type: 'line',
      data: {
        labels,
        datasets: [
          {
            label: ov?.seriesByCheckin ? 'Check-ins (J-7)' : 'Réservations (J-7)',
            data,
            fill: false,
            tension: 0.3,
            borderWidth: 2,
            pointRadius: 3
          },
          {
            label: 'Projection (7 jours)',
            data: this.naiveForecast(data, 7),
            borderDash: [6, 4],
            tension: 0.2,
            borderWidth: 2,
            pointRadius: 0
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: { legend: { display: true } },
        scales: {
          x: { ticks: { maxRotation: 0, autoSkip: true } },
          y: { beginAtZero: true }
        }
      }
    };

    const el = document.getElementById('seriesChart') as HTMLCanvasElement | null;
    if (!el) return;
    this.chart = new Chart(el.getContext('2d')!, cfg);
  }

  private naiveForecast(series: number[], days: number): number[] {
    if (!series.length) return [];
    const tail = series.slice(-3);
    const lastAvg = tail.reduce((a,b)=>a+b,0) / tail.length;
    const pad = new Array(series.length).fill(null as any);
    const proj = Array.from({length: days}, () => lastAvg);
    return pad.concat(proj) as unknown as number[];
  }

  private loadHousekeepingToday(): void {
    if (!this.selectedHotelId) return;

    this.housekeepingService.getToday(this.selectedHotelId, this.tz)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (tasks) => {
          this.tasksToday = tasks || [];
          this.hkCounts.arrivals   = this.tasksToday.filter(t => t.type === 'ARRIVAL').length;
          this.hkCounts.departures = this.tasksToday.filter(t => t.type === 'DEPARTURE').length;
          this.hkCounts.stayovers  = this.tasksToday.filter(t => t.type === 'STAYOVER').length;
        },
        error: (err) => {
          console.error(err);
          this.tasksToday = [];
          this.hkCounts = { arrivals: 0, departures: 0, stayovers: 0 };
        }
      });
  }

  logout() { this.keycloak.logout('http://localhost:4200'); }

  goToPending(): void {
    if (!this.selectedHotelId) { alert('Veuillez sélectionner un hôtel.'); return; }
    this.router.navigate(['/admin/reservations-en-attente'], { queryParams: { hotelId: this.selectedHotelId } });
  }
  goToAjouterChambre(): void {
    this.router.navigate(['/admin/ajouter-chambre']);
  }
}
