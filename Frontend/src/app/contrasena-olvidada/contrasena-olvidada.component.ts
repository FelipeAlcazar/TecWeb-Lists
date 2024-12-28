import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-contrasena-olvidada',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './contrasena-olvidada.component.html',
  styleUrls: ['./contrasena-olvidada.component.css']
})
export class ContrasenaOlvidadaComponent {
  recoveryForm: FormGroup;
  submitted = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  constructor(private formBuilder: FormBuilder, private router: Router, private http: HttpClient) {
    this.recoveryForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit() {
    this.submitted = true;

    if (this.recoveryForm.invalid) {
      return;
    }

    const email = this.recoveryForm.controls['email'].value;
    this.http.post('http://localhost:9000/users/sendRecoveryEmail', { email }).subscribe({
      next: (response: any) => {
        console.log('Response from backend:', response); // Log the response
        this.successMessage = response.message; // Use the response message from the backend
        this.errorMessage = null; // Clear any previous error message
      },
      error: (error) => {
        console.error('Error sending recovery email:', error); // Log the error
        this.errorMessage = 'Failed to send recovery email. Please try again later.';
        this.successMessage = null; // Clear any previous success message
      }
    });
  }

  onReset() {
    this.submitted = false;
    this.recoveryForm.reset();
    this.router.navigate(['/Login']); // Navigate to the login page
  }
}