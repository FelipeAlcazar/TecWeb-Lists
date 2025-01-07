import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { producto } from '../models/producto.model';
import { ListaService } from '../lista.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ManagerService } from '../manager.service';
import { UserService } from '../user.service'; // Import UserService
import { EmailUsuario } from '../models/emailusuario.model';

@Component({
  selector: 'app-detalle-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './detalle-lista.component.html',
  styleUrls: ['./detalle-lista.component.css']
})
export class DetalleListaComponent implements OnInit, AfterViewInit, OnDestroy {
  nuevoProducto: string = '';
  unidadesPedidas: number = 0;
  unidadesCompradas: number = 0;
  producto: producto = new producto();
  idLista?: string = "";
  misProductos: producto[] = [];
  mostrarModal: boolean = false;
  indiceSeleccionado: number = 0;
  mostrarInvitarUsuario: boolean = false;
  correoInvitado: string = '';
  mostrarEnlaceModal: boolean = false;
  enlaceInvitacion: string = '';
  invitados: EmailUsuario[] = [];
  private ws: WebSocket | undefined;

  constructor(
    private listaService: ListaService,
    public route: ActivatedRoute,
    private router: Router,
    private manager: ManagerService,
    private userService: UserService // Inject UserService
  ) {
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

    this.connectWebSocket();
  }

  ngOnDestroy(): void {
    if (this.ws) {
      this.ws.close();
    }
  }

  connectWebSocket(): void {
    const email = this.userService.getCurrentUserEmail();
    this.ws = new WebSocket(`ws://localhost:80/wsListas?email=${email}`);
    console.log('WebSocket created for:', email);

    this.ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      if (message.tipo === 'actualizacionDeLista' && message.idLista === this.idLista) {
        // Handle the list update
        this.updateProductList(message);
      }
    };

    this.ws.onclose = (event) => {
      console.log('WebSocket closed:', event);
    };

    this.ws.onerror = (event) => {
      console.error('WebSocket error:', event);
    };
  }

  updateProductList(message: any): void {
    const updatedProduct = this.misProductos.find(p => p.nombre === message.nombre);
    if (updatedProduct) {
      updatedProduct.unidadesCompradas = message.unidadesCompradas;
      updatedProduct.unidadesPedidas = message.unidadesPedidas;
    } else {
      const newProduct = new producto();
      newProduct.id = message.id; // Ensure the id is set
      newProduct.nombre = message.nombre;
      newProduct.unidadesCompradas = message.unidadesCompradas;
      newProduct.unidadesPedidas = message.unidadesPedidas;
      this.misProductos.push(newProduct);
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
    );
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
        alert('Error al comprar producto: comprueba la cantidad comprada');
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
    this.obtenerInvitados();
    this.mostrarInvitarUsuario = true;
  }

  cerrarInvitarUsuarioModal() {
    this.mostrarInvitarUsuario = false;
  }

  obtenerInvitados() {
    this.listaService.obtenerInvitados(this.idLista!).subscribe(
      (invitados) => {
        this.invitados = invitados;
      },
      (error) => {
        console.error('Error al obtener los invitados:', error);
      }
    );
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

  eliminarInvitado(indice: number) {
    const email = this.invitados[indice].email;
    this.listaService.eliminarInvitado(this.idLista!, email).subscribe(
      (response) => {
        this.invitados.splice(indice, 1);
      },
      (error) => {
        console.error('Error al eliminar el invitado:', error);
      }
    );
  }

  cerrarEnlaceModal() {
    this.mostrarEnlaceModal = false;
  }
}