import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-map-overlay',
  templateUrl: './map-overlay.component.html',
  styleUrls: ['./map-overlay.component.scss']
})
export class MapOverlayComponent implements OnInit {
  latitude!: number;
  longitude!: number;
  nomHotel!: string;

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.latitude = Number(params['lat']);
      this.longitude = Number(params['lng']);
      this.nomHotel = params['nom'] ?? 'Hôtel';
    });
  }
}
