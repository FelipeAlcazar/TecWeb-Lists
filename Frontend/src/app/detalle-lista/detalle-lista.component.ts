import { Component } from '@angular/core';
import { producto } from '../models/producto.model';
import { ListaService } from '../lista.service';
import { ManagerService } from '../manager.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';

@Component({
  selector: 'app-detalle-lista',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './detalle-lista.component.html',
  styleUrl: './detalle-lista.component.css'
})
export class DetalleListaComponent {
  nuevoProducto : string='';
  unidadesPedidas : number=0;
  unidadesCompradas: number=0;
  producto : producto = new producto;
  idLista? : string="";
  misProductos : producto[] = [];
  

  constructor(private listaService: ListaService, public route: ActivatedRoute){
    this.route.paramMap.subscribe(params => {
      this.idLista = params.get('id') || ''; // Proporcionar un valor predeterminado
      // Ahora puedes usar this.idLista para cargar los detalles de la lista
    });
  }

  ngAfterViewInit(): void {
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

  aniadirProducto(){
    console.log('voy a almacenar producto');
    this.producto.crearProducto (this.nuevoProducto,this.unidadesPedidas, this.unidadesCompradas);
    this.listaService.aniadirProducto(this.idLista!,this.producto).subscribe(
      (response) => {
        console.log('producto agregado correctamente:', response);
      },
      (error) => {
        console.error('Error al almacenar el producto:', error);
      }
    );;
  }
}
