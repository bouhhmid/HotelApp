import { Component, OnInit } from '@angular/core';
import { ChambreService } from 'src/app/services/chambre.service';
import { HotelService } from 'src/app/services/hotel.service';
import { TypeChambre, VueChambre, ChambreCreateDTO } from 'src/app/models/Chambre';
@Component({
  selector: 'app-ajouter-chambre',
  templateUrl: './ajouter-chambre.component.html',
  styleUrls: ['./ajouter-chambre.component.scss']
})
export class AjouterChambreComponent implements OnInit {
  hotelId!: number;

  // 🔹 liste pour le <select>
  typeOptions: TypeChambre[] = ['SIMPLE', 'DOUBLE', 'SUITE'];
  vueOptions: VueChambre[] = ['INTERIEUR', 'MER', 'JARDIN', 'PISCINE'];

  // 🔹 unique source de vérité pour le formulaire
  form = {
    numero: '',
    typeChambre: 'SIMPLE' as TypeChambre,
    prixBase: 0,
    description: '',
    capaciteAdulte: 0,
    capaciteEnfant: 0,
    imageUrl: '',
    vue: 'INTERIEUR' as VueChambre
  };

  constructor(
    private chambreService: ChambreService,
    private hotelService: HotelService
  ) {}

  ngOnInit(): void {
    this.hotelService.getHotelDeAdmin().subscribe({
      next: (hotel) => { this.hotelId = hotel.id; },
      error: (err) => {
        alert('Erreur lors de la récupération de l’hôtel');
        console.error(err);
      }
    });
  }

  ajouterChambre(): void {
    if (!this.hotelId) { alert('Hôtel non trouvé'); return; }

    // sécuriser les nombres
    this.form.prixBase       = +this.form.prixBase;
    this.form.capaciteAdulte = +this.form.capaciteAdulte;
    this.form.capaciteEnfant = +this.form.capaciteEnfant;

    // ✅ construire un payload AVEC au moins UNE option
    const payload: ChambreCreateDTO = {
      numero: this.form.numero,
      typeChambre: this.form.typeChambre,
      description: this.form.description,
      imageUrl: this.form.imageUrl,
      capaciteAdulte: this.form.capaciteAdulte,
      capaciteEnfant: this.form.capaciteEnfant,
      options: [{
        typeChambre: this.form.typeChambre,
        vue: this.form.vue,
        prixParNuitParPersonne: this.form.prixBase,
        actif: true
      }]
    };

    this.chambreService.ajouterChambre(this.hotelId, payload).subscribe({
      next: () => alert('Chambre ajoutée avec succès'),
      // le backend renvoie { error: '...' }
      error: (err) => {
        console.error(err);
        alert(err?.error?.error ?? 'Erreur lors de l’ajout de la chambre');
      }
    });
  }
}
