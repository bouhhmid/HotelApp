import { Component } from '@angular/core';
import { KeycloakService } from 'keycloak-angular';
@Component({
  selector: 'app-superadmin-dashboard',
  templateUrl: './superadmin-dashboard.component.html',
  styleUrls: ['./superadmin-dashboard.component.css']
})
export class SuperadminDashboardComponent {
  constructor(private keyclockservice:KeycloakService) {}
  public logout(): void {
    this.keyclockservice.logout('http://localhost:4200');
  }
}
