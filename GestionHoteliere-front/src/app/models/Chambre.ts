export type TypeChambre = 'SIMPLE' | 'DOUBLE' | 'SUITE';
export type VueChambre  = 'MER' | 'JARDIN' | 'PISCINE' | 'INTERIEUR';

export interface ChambreOptionDTO {
  id: number;
  typeChambre: TypeChambre;
  vue: VueChambre;
  prixParNuitParPersonne: number;
  actif: boolean;
}
export interface Chambre {
  id?: number;
  numero: string;
  prixBase: number;
  capaciteAdulte: number;
  typeChambre: TypeChambre;
  capaciteEnfant: number;
  imageUrl: string;
  dispo: boolean;
  description: string;
  photos?: string[];
  options?: ChambreOptionDTO[];
}

export interface ChambreOptionCreateDTO {
  id?: number; // important: pas d’id à la création
  typeChambre: TypeChambre;
  vue: VueChambre;
  prixParNuitParPersonne: number;
  actif: boolean;
}
export interface ChambreCreateDTO {
  numero: string;
  typeChambre: TypeChambre;
  description?: string;
  imageUrl?: string;
  capaciteAdulte: number;
  capaciteEnfant: number;
  options: ChambreOptionCreateDTO[]; // >= 1
}
