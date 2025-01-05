import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ValidatorFn, ReactiveFormsModule } from '@angular/forms';
import { UserService } from '../user.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register1',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register1.component.html',
  styleUrls: ['./register1.component.css']
})
export class Register1Component {
  registerForm: FormGroup;
  submitted = false;
  respuestaOK = false;
  emailConfirmationPending = false;
  passwordHasUpperCase = false;
  passwordHasLowerCase = false;
  passwordHasNumeric = false;

  constructor(private formBuilder: FormBuilder, private userService: UserService, private router: Router) {
    this.registerForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      pwd1: ['', [Validators.required, Validators.minLength(8), createPasswordStrengthValidator()]],
      pwd2: ['', [Validators.required]]
    }, {
      validator: mustMatch('pwd1', 'pwd2')
    });
  }

  onSubmit() {
    this.submitted = true;
    if (this.registerForm.invalid) {
      console.warn("Formulario invalido");
    } else {
      console.log("todo OK" + JSON.stringify(this.registerForm.value, null, 2));
      this.userService.register1(
        this.registerForm.controls['email'].value,
        this.registerForm.controls['pwd1'].value,
        this.registerForm.controls['pwd2'].value
      ).subscribe((data) => {
        console.log(JSON.stringify(data));
        this.respuestaOK = true;

        // Send confirmation email
        this.userService.sendConfirmationEmail(this.registerForm.controls['email'].value).subscribe((response) => {
          console.log('Confirmation email sent:', response);
          this.emailConfirmationPending = true;
        }, (error) => {
          console.error('Error sending confirmation email:', error);
        });

        this.router.navigate(['/GestorListas']).then(() => {
          window.location.reload();
        });
      });
    }
  }

  onReset() {
    this.submitted = false;
    this.registerForm.reset();
    this.respuestaOK = false;
    this.emailConfirmationPending = false;
    this.passwordHasUpperCase = false;
    this.passwordHasLowerCase = false;
    this.passwordHasNumeric = false;
    this.router.navigate(['']); // Navigate to home page
  }

  updatePasswordRequirements() {
    const value = this.registerForm.controls['pwd1'].value;
    this.passwordHasUpperCase = /[A-Z]+/.test(value);
    this.passwordHasLowerCase = /[a-z]+/.test(value);
    this.passwordHasNumeric = /[0-9]+/.test(value);
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

export function mustMatch(controlName: string, matchingControlName: string): ValidatorFn {
  return (formGroup: AbstractControl): ValidationErrors | null => {
    const control = formGroup.get(controlName);
    const matchingControl = formGroup.get(matchingControlName);
    if (matchingControl?.errors && !matchingControl.errors['mustMatch']) {
      return null;
    }
    if (control?.value !== matchingControl?.value) {
      matchingControl?.setErrors({ mustMatch: true });
    } else {
      matchingControl?.setErrors(null);
    }
    return null;
  };
}