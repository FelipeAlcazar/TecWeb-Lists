import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ListaService } from '../lista.service';
import { lista } from '../models/lista.model';
import { Router, RouterModule } from '@angular/router';
import { ManagerService } from '../manager.service';

@Component({
  selector: 'app-gestor-listas',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './gestor-listas.component.html',
  styleUrl: './gestor-listas.component.css',
})
export class GestorListasComponent {
  nuevaLista?: string;
  misListas: lista[] = [];
  listaCreada: lista = new lista();
  constructor(
    private service: ListaService,
    private router: Router,
    public manager: ManagerService
  ) {}

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    this.inicializarComponent();
  }

  inicializarComponent(): void {
    if (typeof window !== 'undefined' && window.localStorage) {
      this.service.obtenerListas().subscribe(
        (listas) => {
          this.misListas = listas;
        },
        (error) => {
          console.error('Error al obtener las lista', error);
        }
      )
    }
  }

  agregarLista() {
    console.log('Voy a almacenar una lista nueva: ' + this.nuevaLista);

    this.service.crearLista(this.nuevaLista!).subscribe(
      (response) => {
        this.listaCreada = response;
        console.log('Lista creada', response);
        this.misListas.push(this.listaCreada);
      },
      (error) => {
        alert('Error al crear la lista: los usuarios no premium no pueden crear más de 2 listas');
        console.error('Error al crear la lista', error);
      }
    );
  }

  agregarProducto(indice: number) {
    this.manager.listaSeleccionada = this.misListas[indice];
    this.router.navigate(['/DetalleLista']);
  }

  eliminarLista(indice: number) {
    const idLista = this.misListas[indice].id;
    console.log('Voy a eliminar la lista con id: ' + idLista);
    this.service.eliminarLista(idLista).subscribe(
      (response) => {
        this.misListas.splice(indice, 1);
      },
      (error) => {
        console.error('Error al eliminar la lista', error);
        if (error.status === 403) {
          window.alert('Solo el propietario puede eliminar esta lista.');
        } else {
          window.alert('Error al eliminar la lista: ' + 'debes eliminar los productos primero.');
        }
      }
    );
  }
}