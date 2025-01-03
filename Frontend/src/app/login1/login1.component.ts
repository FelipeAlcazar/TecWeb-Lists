import { Component } from '@angular/core';
import { UserService } from '../user.service';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule, Validators, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login1',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login1.component.html',
  styleUrls: ['./login1.component.css']
})
export class Login1Component {
  loginForm: FormGroup;
  submitted = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(private formBuilder: FormBuilder, private userService: UserService, private router: Router) {    
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      pwd: ['', [Validators.required]]
    });
  }

  onSubmit() {
    this.submitted = true;
    this.errorMessage = null; // Reset error message
    this.successMessage = null; // Reset success message
    if (this.loginForm.invalid) {
      this.errorMessage = "Comprueba que has introducido correctamente los datos del formulario."; // Set error message
    } else {
      this.userService.login1(this.loginForm.controls['email'].value, this.loginForm.controls['pwd'].value).subscribe({
        next: (data) => {
          this.successMessage = '¡Inicio de sesión exitoso!';
          this.router.navigate(['/GestorListas']).then(() => {
            window.location.reload();
          });

        },
        error: (error) => {
          console.error('Login error:', error); // Log the error for debugging
          this.errorMessage = error.error?.error || 'Usuario no encontrado o contraseña incorrecta.';
        }
      });
    }
  }

  onReset() {
    this.submitted = false;
    this.loginForm.reset();
    this.errorMessage = null; // Reset error message
    this.successMessage = null; // Reset success message
    this.router.navigate(['']); // Navigate to home page
  }
}