import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { lista } from './models/lista.model';
import { producto } from './models/producto.model';
import { Observable, from} from 'rxjs';
import { UserService } from './user.service';
import { switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ListaService {

  // This is the url of the web api that we will use to send the data to the server.
  private apiUrl = "http://localhost:80/listas"
  constructor(private http:HttpClient, private userService: UserService) { }

  crearLista(nombre:string){
    const token = localStorage.getItem('authToken');

    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'authToken': token || ''
  });

  return this.http.post<any>(this.apiUrl + "/crearLista", nombre , { headers });
}

  aniadirProducto(idLista: string, producto: producto): Observable<lista> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'idLista': idLista  // Se pasa el IdLista en la cabecera
    });

    return this.http.post<any>(this.apiUrl+"/addProducto", producto, { headers });
  }

  obtenerListas(): Observable<lista[]> {
    return from(this.userService.checkCookie()).pipe(
      switchMap(() => {
        const token = localStorage.getItem('authToken');
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'authToken': token || ''
        });
        return this.http.get<lista[]>(`${this.apiUrl}/obtenerListas`, { headers });
      })
    );
  }

  eliminarLista(idLista: string): void {
    const token = localStorage.getItem('authToken');
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'authToken': token || '',
    });
    this.http.delete<any>(`${this.apiUrl}/eliminarLista/${idLista}`, { headers }).subscribe(
      response => {
        console.log('Lista eliminada exitosamente', response);
      },
      error => {
        console.error('Error al eliminar la lista', error);
      }
    );
  }
}
