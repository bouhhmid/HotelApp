import { Component, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild, ElementRef } from '@angular/core';
import { Subject, forkJoin, of } from 'rxjs';
import { catchError, takeUntil } from 'rxjs/operators';
import { AdminStatsService, AiInsightsDTO } from 'src/app/services/admin-stats.service';
import { Chart, ChartConfiguration, registerables } from 'chart.js';

Chart.register(...registerables);

type SeriesPoint = { date: string; value: number };

@Component({
  selector: 'app-ai-insight-card',
  templateUrl: './ai-insight-card.component.html',
  styleUrls: ['./ai-insight-card.component.scss']
})
export class AiInsightCardComponent implements OnChanges, OnDestroy {
  @Input() hotelId?: number;
  @Input() tz = 'Africa/Tunis';
  @Input() windowDays = 7;

  @ViewChild('spark', { static: false }) sparkRef?: ElementRef<HTMLCanvasElement>;

  loading = false;
  error = '';
  insight?: AiInsightsDTO | null;

  private destroy$ = new Subject<void>();
  private chart?: Chart;

  constructor(private stats: AdminStatsService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['hotelId'] && this.hotelId) {
      this.fetch();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next(); this.destroy$.complete();
    this.chart?.destroy();
  }

  private fetch(): void {
    if (!this.hotelId) return;
    this.loading = true;
    this.error = '';

    // On charge l’insight ET la série (pour le petit sparkline) en parallèle
    const ov$ = this.stats.getOverview({
      hotelId: this.hotelId,
      seriesBy: 'reservation',
      windowDays: this.windowDays,
      round: 1,
      tz: this.tz,
      distinct: true,
      includeTypes: false
    }).pipe(catchError(() => of(null as any)));

    const ai$ = this.stats.getAiInsights(this.hotelId, this.windowDays, this.tz)
      .pipe(catchError(() => of(null as unknown as AiInsightsDTO)));

    forkJoin([ov$, ai$]).pipe(takeUntil(this.destroy$)).subscribe({
      next: ([ov, ai]) => {
        this.insight = ai;

        // Sparkline
        const series: SeriesPoint[] =
          ov?.series
            ?? (ov?.reservations7J || []).map((p: any) => ({ date: p.x, value: p.y }))  // compat
            ?? [];

        setTimeout(() => this.renderSpark(series), 0);
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.error = 'Impossible de charger les insights.';
        this.insight = null;
      }
    });
  }

  private renderSpark(series: SeriesPoint[]) {
    this.chart?.destroy();
    const el = this.sparkRef?.nativeElement;
    if (!el) return;

    const labels = series.map(p => p.date);
    const data   = series.map(p => p.value);

    const cfg: ChartConfiguration<'line'> = {
      type: 'line',
      data: { labels, datasets: [{ data, tension: 0.3, borderWidth: 2, pointRadius: 0, fill: false }] },
      options: {
        responsive: true, maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip: { mode: 'index', intersect: false } },
        scales: { x: { display: false }, y: { display: false } }
      }
    };

    this.chart = new Chart(el.getContext('2d')!, cfg);
  }
}
