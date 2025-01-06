import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UserService } from './user.service';
import { PagosService } from './pagos.service';
import { PaymentComponent } from './payment/payment.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterModule, PaymentComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'TecWeb-Lists';
  isAuthenticated = false;
  showLogoutConfirm = false;
  hasPaid = false;
  showPaymentForm = false;

  constructor(private userService: UserService, private pagosService: PagosService, private router: Router) {}

  ngOnInit() {
    this.userService.checkCookie().then(() => {
      this.isAuthenticated = true;
      this.checkUserHasPaid();
      this.redirectAuthenticatedUser();
      if (this.router.url === '/') {
        this.router.navigate(['/GestorListas']);
      }
    }).catch(() => {
      this.isAuthenticated = false;
    });
  }

  checkUserHasPaid() {
    if (this.isAuthenticated) {
      this.userService.getUserHasPaid().subscribe(
        response => {
          this.hasPaid = response.body?.hasPaid || false;
        },
        error => {
          console.error('Get user status error:', error);
        }
      );
    }
  }

  redirectAuthenticatedUser() {
    const currentUrl = this.router.url;
    if (this.isAuthenticated && (currentUrl === '/Login' || currentUrl === '/Register')) {
      this.router.navigate(['/GestorListas']);
    }
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