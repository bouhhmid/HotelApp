// src/app/housekeeping/rooms-board/rooms-board.component.ts
import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { BoardServiceService } from 'src/app/services/board-service.service';
import { ChambreService } from 'src/app/services/chambre.service';
import { RoomBoardDTO } from 'src/app/models/board';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-rooms-board',
  templateUrl: './rooms-board.component.html',
  styleUrls: ['./rooms-board.component.scss']
})
export class RoomsBoardComponent implements OnInit {
  @Input() hotelId!: number;

  displayedColumns = [
    'numero', 'type', 'occupee', 'checkout', 'hk', 'last', 'actions'
  ];
  data = new MatTableDataSource<RoomBoardDTO>([]);
  loading = false;
  filterValue = '';

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private board: BoardServiceService,
    private chambre: ChambreService,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void { 
    console.log('hotelId reçu :', this.hotelId); // DEBUG
    this.load(); }

  load() {
    this.loading = true;
    this.board.getBoard(this.hotelId).subscribe({
      next: rows => {
        this.data.data = rows;
        setTimeout(() => {
          this.data.paginator = this.paginator;
          this.data.sort = this.sort;
        });
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.snack.open(err?.error?.message || 'Erreur chargement tableau', 'Fermer', { duration: 3000 });
      }
    });
  }

  applyFilter(v: string) {
    this.filterValue = v.trim().toLowerCase();
    this.data.filter = this.filterValue;
  }

  markDirty(row: RoomBoardDTO) {
    this.chambre.markDirty(row.chambreId).subscribe({
      next: () => { row.etatChambre = 'DIRTY'; this.snack.open('Marquée SALE', 'OK', { duration: 1500 }); },
      error: e => this.snack.open(e?.error?.message || 'Erreur', 'Fermer', { duration: 3000 })
    });
  }

  markClean(row: RoomBoardDTO) {
    this.chambre.markClean(row.chambreId).subscribe({
      next: () => { row.etatChambre = 'CLEAN'; this.snack.open('Marquée PROPRE', 'OK', { duration: 1500 }); },
      error: e => this.snack.open(e?.error?.message || 'Erreur', 'Fermer', { duration: 3000 })
    });
  }
}
