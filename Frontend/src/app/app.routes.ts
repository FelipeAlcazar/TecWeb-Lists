import { Routes } from '@angular/router';
import { Login1Component } from './login1/login1.component';
import { Register1Component } from './register1/register1.component';
import { GestorListasComponent } from './gestor-listas/gestor-listas.component';
import { DetalleListaComponent } from './detalle-lista/detalle-lista.component';
import { ContrasenaOlvidadaComponent } from './contrasena-olvidada/contrasena-olvidada.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';

export const routes: Routes = [
    { path: 'Login', component: Login1Component },
    { path: 'Register', component: Register1Component },
    { path: 'GestorListas', component: GestorListasComponent },
    { path: 'DetalleLista', component: DetalleListaComponent },
    { path: 'contrasenaOlvidada', component: ContrasenaOlvidadaComponent },
    { path: 'resetPassword', component: ResetPasswordComponent }
];
