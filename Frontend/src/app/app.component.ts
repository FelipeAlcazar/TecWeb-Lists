import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Register1Component } from "./register1/register1.component";
import { Login1Component } from './login1/login1.component';
import { GestorListasComponent } from './gestor-listas/gestor-listas.component';
import { DetalleListaComponent } from './detalle-lista/detalle-lista.component';
import { UserService } from './user.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'TecWeb-Lists';
  isAuthenticated = false;
  showLogoutConfirm = false;

  constructor(private userService: UserService, private router: Router) {

  }

  ngOnInit(): void {
    this.userService.checkCookie()
      .then(() => {
        this.isAuthenticated = true;
      })
      .catch(error => {
        this.isAuthenticated = false;
        console.error('Check cookie error:', error);
      });
  }

  logout() {
    this.showLogoutConfirm = true;
  }

  confirmLogout() {
    this.userService.logout().subscribe({
      next: () => {
        localStorage.removeItem('authToken');
        this.isAuthenticated = false;
        this.showLogoutConfirm = false;
        this.router.navigate(['']).then(() => {
          window.location.reload(); // Force a page refresh
        });
      },
      error: (error) => {
        console.error('Logout error:', error);
      }
    });
  }

  cancelLogout() {
    this.showLogoutConfirm = false;
  }
}