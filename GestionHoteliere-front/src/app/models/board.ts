// src/app/models/board.ts
export interface RoomBoardDTO {
  chambreId: number;
  numero: string;
  typeChambre: string | null;
  etatChambre: 'CLEAN' | 'DIRTY' | string;
  lastCleanedAt?: string | null;

  occupee: boolean;
  reservationId?: number | null;
  dateDebut?: string | null; // ISO
  dateFin?: string | null;   // ISO (checkout)
  client?: string | null;
}
