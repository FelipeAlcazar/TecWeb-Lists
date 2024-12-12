import { Component } from '@angular/core';
import { UserService } from '../user.service';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule, Validators, FormBuilder, ValidatorFn, AbstractControl, ValidationErrors } from '@angular/forms';

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

  constructor(private formBuilder: FormBuilder, private userService: UserService) {    
    this.loginForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      pwd: ['', [Validators.required, Validators.minLength(8), createPasswordStrengthValidator()]]
    });
  }

  onSubmit() {
    this.submitted = true;
    this.errorMessage = null; // Reset error message
    this.successMessage = null; // Reset success message
    if (this.loginForm.invalid) {
      console.warn("Formulario invalido");
    } else {
      this.userService.login1(this.loginForm.controls['email'].value, this.loginForm.controls['pwd'].value).subscribe({
        next: (data) => {
          this.successMessage = '¡Inicio de sesión exitoso!';
        },
        error: (error) => {
          this.errorMessage = error.error.error || 'Ocurrió un error durante el inicio de sesión';
        }
      });
    }
  }

  onReset() {
    this.submitted = false;
    this.loginForm.reset();
    this.errorMessage = null; // Reset error message
    this.successMessage = null; // Reset success message
  }
}

export function createPasswordStrengthValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (!value) {
      return null;
    }
    const hasUpperCase = /[A-Z]+/.test(value);
    const hasLowerCase = /[a-z]+/.test(value);
    const hasNumeric = /[0-9]+/.test(value);
    const passwordValid = hasUpperCase && hasLowerCase && hasNumeric;
    return !passwordValid ? { passwordStrength: true } : null;
  };
}