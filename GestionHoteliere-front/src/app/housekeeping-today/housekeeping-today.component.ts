import { Component, Input, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { Subject, takeUntil } from 'rxjs';
import { HousekeepingService, HousekeepingTask } from 'src/app/services/housekeeping.service';

@Component({
  selector: 'app-housekeeping-today',
  templateUrl: './housekeeping-today.component.html',
  styleUrls: ['./housekeeping-today.component.scss']
})
export class HousekeepingTodayComponent implements OnChanges, OnDestroy {
  @Input() hotelId?: number;
  @Input() tz = 'Africa/Tunis';

  loading = false;
  error = '';
  tasks: HousekeepingTask[] = [];
  counts = { arrivals: 0, departures: 0, stayovers: 0 };

  private destroy$ = new Subject<void>();

  constructor(private hk: HousekeepingService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['hotelId'] && this.hotelId) {
      this.fetch();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private fetch(): void {
    if (!this.hotelId) return;
    this.loading = true;
    this.error = '';
    this.hk.getToday(this.hotelId, this.tz)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: list => {
          this.tasks = (list || []).slice().sort(this.sorter);
          this.counts.arrivals   = this.tasks.filter(t => t.type === 'ARRIVAL').length;
          this.counts.departures = this.tasks.filter(t => t.type === 'DEPARTURE').length;
          this.counts.stayovers  = this.tasks.filter(t => t.type === 'STAYOVER').length;
          this.loading = false;
        },
        error: () => {
          this.tasks = [];
          this.counts = { arrivals: 0, departures: 0, stayovers: 0 };
          this.error = 'Impossible de charger le housekeeping.';
          this.loading = false;
        }
      });
  }

  private sorter(a: HousekeepingTask, b: HousekeepingTask) {
    const pa = a.priority ?? 99, pb = b.priority ?? 99;
    if (pa !== pb) return pa - pb;
    if (a.dueTime !== b.dueTime) return (a.dueTime || '').localeCompare(b.dueTime || '');
    return (a.room || '').localeCompare(b.room || '');
  }
}
