import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { lista } from './models/lista.model';
import { producto } from './models/producto.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ListaService {

  // This is the url of the web api that we will use to send the data to the server.
  private apiUrl = "http://localhost:80/listas"
  constructor(private http:HttpClient) { }

  crearLista(nombre:string){

    return this.http.post<any>(this.apiUrl+"/crearLista", nombre)
  }

  aniadirProducto(idLista: string, producto: producto): Observable<lista> {
    let apiUrlEspecifica= this.apiUrl+"/addProducto";
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'idLista': idLista  // Se pasa el IdLista en la cabecera
    });

    return this.http.post<any>(apiUrlEspecifica, producto, { headers });
  }
}
