// keycloak-init.factory.ts
import { KeycloakService } from 'keycloak-angular';

export function initializeKeycloak(keycloak: KeycloakService) {
  return () =>
    keycloak.init({
      config: {
        url: 'http://localhost:9090',     // URL de Keycloak
        realm: 'Gestion-Hoteliere',       // Ton realm
        clientId: 'bsn'        // Nom du client Angular dans Keycloak
      },
      initOptions: {
  onLoad: 'check-sso',
  silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
  checkLoginIframe: false
}

    });
}
