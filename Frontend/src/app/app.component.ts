import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Register1Component } from "./register1/register1.component";
import { Login1Component } from './login1/login1.component';
import { GestorListasComponent } from './gestor-listas/gestor-listas.component';
import { DetalleListaComponent } from './detalle-lista/detalle-lista.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, Register1Component, Login1Component, GestorListasComponent, DetalleListaComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'sslfe';
}
