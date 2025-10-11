import { Component, OnInit } from '@angular/core';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'GestionHoteliere';

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.isLoggedIn().then((loggedIn) => {
      if (loggedIn) {
        this.authService.refreshTokenAndRedirectIfNeeded();
        this.authService.syncUser();
      }
    });
  }

  logout(): void {
    this.authService.logout();
  }
}
