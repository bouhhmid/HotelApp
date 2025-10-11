import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { KeycloakService } from 'keycloak-angular';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  constructor(
    private keycloakService: KeycloakService,
    private router: Router,
    private http: HttpClient
  ) {}

  // 🔁 Rafraîchit le token et redirige en fonction du rôle à jour
  public async refreshTokenAndRedirectIfNeeded(): Promise<void> {
    try {
      const refreshed = await this.keycloakService.updateToken(5);
      if (refreshed) {
        console.log('🔁 Token rafraîchi.');
      } else {
        console.log('✅ Token encore valide.');
      }
      this.redirectUserBasedOnRole();
    } catch (error) {
      console.error('❌ Erreur lors du refresh du token', error);
      this.logout();
    }
  }

  // Redirection selon rôle
  public redirectUserBasedOnRole(): void {
    const roles = this.keycloakService.getUserRoles();
    console.log("Roles de l'utilisateur :", roles);

    if (roles.includes('ROLE_SUPERADMIN')) {
      this.router.navigate(['/superadmin/dashboard']);
    } else if (roles.includes('ROLE_ADMIN')) {
      this.router.navigate(['/admin/dashboard']);
      
    } else if (roles.includes('ROLE_CLIENT')) {
      this.router.navigate(['/home']);
    } else {
      this.router.navigate(['/unauthorized']);
    }
  }

  public async isLoggedIn(): Promise<boolean> {
    return this.keycloakService.isLoggedIn();
  }

  public getUserProfile(): Promise<any> {
    return this.keycloakService.loadUserProfile();
  }

  public logout(): void {
    this.keycloakService.logout('http://localhost:4200');
  }

  public login(): void {
    this.keycloakService.login();
  }

  public register(): void {
    this.keycloakService.register();
    console.log("🔗 Redirection vers la page d'inscription...");
  }

  public hasRole(role: string): boolean {
    const roles = this.keycloakService.getUserRoles();
    return roles.includes(role);
  }

  public syncUser(): void {
    this.http.get('http://localhost:8081/api/users/me').subscribe({
      next: (user) => console.log("✅ Utilisateur synchronisé :", user),
      error: (err) => console.error("❌ Erreur de synchronisation utilisateur :", err)
    });
  }
}
