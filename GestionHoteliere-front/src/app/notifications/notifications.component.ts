import { Component, OnInit } from '@angular/core';
import { NotificationsService } from 'src/app/services/notification.service';
import { Notification, Page } from 'src/app/services/notification.service';
@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.scss']
})
export class NotificationsComponent implements OnInit {
  pageIndex = 0;
  pageSize = 10;
  totalPages = 0;
  totalElements = 0;
  rows: Notification[] = [];
  loading = false;

  constructor(private notif: NotificationsService) {}

  ngOnInit(): void {
    this.load();
  }

  load(page = this.pageIndex): void {
    this.loading = true;
this.notif.page(page, this.pageSize).subscribe({
      next: (p: Page<Notification>) => {
        this.rows = p.content || [];
        this.pageIndex = p.number;
        this.pageSize = p.size;
        this.totalPages = p.totalPages;
        this.totalElements = p.totalElements;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  markAsRead(n: Notification): void {
    this.notif.markAsRead(n.id).subscribe({
      next: () => this.load(),
      error: () => alert('Erreur lors de la mise à jour')
    });
  }

  prev(): void { if (this.pageIndex > 0) this.load(this.pageIndex - 1); }
  next(): void { if (this.pageIndex + 1 < this.totalPages) this.load(this.pageIndex + 1); }
}
