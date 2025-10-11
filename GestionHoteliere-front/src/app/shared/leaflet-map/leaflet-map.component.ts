import {
  Component, Input, AfterViewInit, OnDestroy,
  ElementRef, ViewChild, HostListener
} from '@angular/core';
import * as L from 'leaflet';

// Icônes par défaut (optionnel)
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'assets/marker-icon-2x.png',
  iconUrl:       'assets/marker-icon.png',
  shadowUrl:     'assets/marker-shadow.png'
});

@Component({
  selector: 'app-leaflet-map',
  templateUrl: './leaflet-map.component.html',
  styleUrls: ['./leaflet-map.component.scss']
})
export class LeafletMapComponent implements AfterViewInit, OnDestroy {
  @Input() latitude!: number;
  @Input() longitude!: number;
  @Input() zoom = 15;

  @ViewChild('mapEl', { static: true }) mapEl!: ElementRef<HTMLDivElement>;

  private map!: L.Map;
  private resizeObs?: ResizeObserver;

  ngAfterViewInit(): void {
    // 1) init après rendu
    this.map = L.map(this.mapEl.nativeElement, { zoomControl: true })
      .setView([this.latitude, this.longitude], this.zoom);

    // 2) tuiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; OpenStreetMap'
    }).addTo(this.map);

    // 3) marqueur
    L.marker([this.latitude, this.longitude]).addTo(this.map);

    // 4) recalage (overlay/animations)
    setTimeout(() => this.map.invalidateSize(), 0);
    setTimeout(() => this.map.invalidateSize(), 300);

    // 5) recalage si le conteneur change de taille
    this.resizeObs = new ResizeObserver(() => this.map?.invalidateSize());
    this.resizeObs.observe(this.mapEl.nativeElement);
  }

  @HostListener('window:resize')
  onWinResize() { this.map?.invalidateSize(); }

  ngOnDestroy(): void {
    this.resizeObs?.disconnect();
    if (this.map) this.map.remove();
  }
}
