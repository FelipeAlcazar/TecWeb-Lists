import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { UserService } from '../user.service';

@Component({
  selector: 'app-confirm-email',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './confirm-email.component.html',
  styleUrls: ['./confirm-email.component.css']
})
export class ConfirmEmailComponent implements OnInit {
  message: string = '';
  error: string = '';

  constructor(private route: ActivatedRoute, private userService: UserService, private router: Router) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      if (token) {
        this.userService.confirmEmail(token).subscribe(
          response => {
            this.message = 'Email confirmado exitosamente. Disfruta de la aplicación.';
          },
          error => {
            this.error = 'Error al confirmar el email. Por favor, intenta nuevamente.';
          }
        );
      } else {
        this.error = 'Token no proporcionado.';
      }
    });
  }
}