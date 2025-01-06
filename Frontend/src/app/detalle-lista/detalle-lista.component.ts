import { Component, OnInit } from '@angular/core';
import { producto } from '../models/producto.model';
import { ListaService } from '../lista.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ManagerService } from '../manager.service';

@Component({
  selector: 'app-detalle-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './detalle-lista.component.html',
  styleUrl: './detalle-lista.component.css'
})
export class DetalleListaComponent implements OnInit {
  nuevoProducto: string = '';
  unidadesPedidas: number = 0;
  unidadesCompradas: number = 0;
  producto: producto = new producto;
  idLista?: string = "";
  misProductos: producto[] = [];
  mostrarModal: boolean = false;
  indiceSeleccionado: number = 0;
  mostrarInvitarUsuario: boolean = false;
  correoInvitado: string= '';
  mostrarEnlaceModal: boolean = false;
  enlaceInvitacion: string = '';

  constructor(private listaService: ListaService, public route: ActivatedRoute, private router: Router, private manager: ManagerService) {
    this.idLista = this.manager.listaSeleccionada?.id || ''; // Proporcionar un valor predeterminado
  }

  ngOnInit(): void {
    if (!this.idLista) {
      this.router.navigate(['/GestorListas']);
    }
  }

  ngAfterViewInit(): void {
    if (!this.idLista) {
      return;
    }

    if (typeof window !== 'undefined' && window.localStorage) {
      this.listaService.obtenerProductos(this.idLista!).subscribe(
        (productos) => {
          this.misProductos = productos;
        },
        (error) => {
          console.error('Error al obtener las lista', error);
        }
      )
    }
  }

  aniadirProducto() {
    console.log('voy a almacenar producto');
    this.producto.crearProducto(this.nuevoProducto, this.unidadesPedidas, this.unidadesCompradas);
    this.listaService.aniadirProducto(this.idLista!, this.producto).subscribe(
      (response) => {
        console.log('producto agregado correctamente:', response);
        this.misProductos = response.productos;
      },
      (error) => {
        console.error('Error al almacenar el producto:', error);
      }
    );;
  }

  abrirModal(indice: number) {
    this.indiceSeleccionado = indice;
    this.mostrarModal = true;
  }

  cerrarModal() {
    this.mostrarModal = false;
  }

  comprarProducto(indice: number, unidades: number) {
    this.listaService.comprar(this.misProductos[indice].id, unidades).subscribe(
      (response) => {
        this.misProductos[indice] = response;
        this.cerrarModal();
      },
      (error) => {
        console.error('Error al comprar el producto:', error);
      }
    );
  }

  eliminarProducto(indice: number) {
    console.log('voy a eliminar producto' + this.misProductos[indice].id);
    this.listaService.eliminarProducto(this.misProductos[indice].id).subscribe(
      (response) => {
        this.misProductos.splice(indice, 1);
      },
      (error) => {
        console.error('Error al eliminar el producto:', error);
      }
    );
  }

  mostrarInvitarUsuarioModal() {
    this.mostrarInvitarUsuario = true;
  }

  cerrarInvitarUsuarioModal() {
    this.mostrarInvitarUsuario = false;
  }

  addInvitado() {
    const email = this.correoInvitado;
    this.listaService.addInvitado(this.idLista!, email).subscribe(
      (response) => {
        console.log('Invitado añadido correctamente:', response);
        this.enlaceInvitacion = response;
        this.cerrarInvitarUsuarioModal();
        this.mostrarEnlaceModal = true;
      },
      (error) => {
        console.error('Error al añadir el invitado:', error);
      }
    );
  }

  cerrarEnlaceModal() {
    this.mostrarEnlaceModal = false;
  }
}