import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Register1Component } from "./register1/register1.component";
import { Login1Component } from './login1/login1.component';
import { GestorListasComponent } from './gestor-listas/gestor-listas.component';
import { DetalleListaComponent } from './detalle-lista/detalle-lista.component';
import { UserService } from './user.service';
import { PagosService } from './pagos.service';
import { PaymentComponent } from './payment/payment.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule, Register1Component, Login1Component, GestorListasComponent, DetalleListaComponent, PaymentComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'TecWeb-Lists';
  isAuthenticated = false;
  showLogoutConfirm = false;
  hasPaid = false;
  showPaymentForm = false;

  constructor(private userService: UserService, private pagosService: PagosService, private router: Router) {
    this.userService.checkCookie().subscribe(
      token => {
        if (token.body && token.body.token !== "") {
          this.isAuthenticated = true;
          this.checkUserHasPaid(); // Check payment status after authentication
        } else {
          this.isAuthenticated = false;
        }
      },
      error => {
        this.isAuthenticated = false;
        console.error('Check cookie error:', error);
      }
    );
  }

  ngOnInit() {
    if (this.isAuthenticated) {
      this.checkUserHasPaid();
    }
  }

  checkUserHasPaid() {
    this.userService.getUserHasPaid().subscribe(
      response => {
        this.hasPaid = response.body?.hasPaid || false;
      },
      error => {
        console.error('Get user status error:', error);
      }
    );
  }

  subscribeToPremium() {
    this.showPaymentForm = true;
  }

  onPaymentSuccess() {
    this.showPaymentForm = false;
    this.hasPaid = true;
  }

  onPaymentCancel() {
    this.showPaymentForm = false;
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