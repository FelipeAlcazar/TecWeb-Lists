import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { lista } from './models/lista.model';
import { producto } from './models/producto.model';
import { Observable, from } from 'rxjs';
import { UserService } from './user.service';
import { switchMap } from 'rxjs/operators';
import { EmailUsuario } from './models/emailusuario.model';

@Injectable({
  providedIn: 'root'
})
export class ListaService {

  private apiUrl = "http://localhost:80/listas";

  constructor(private http: HttpClient, private userService: UserService) { }

  crearLista(nombre: string): Observable<any> {
    return from(this.userService.checkCookie()).pipe(
      switchMap(() => {
        const token = localStorage.getItem('authToken');
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'authToken': token || ''
        });
        return this.http.post<any>(`${this.apiUrl}/crearLista`, nombre, { headers });
      })
    );
  }

  aniadirProducto(idLista: string, producto: producto): Observable<lista> {
    return from(this.userService.checkCookie()).pipe(
      switchMap(() => {
        const token = localStorage.getItem('authToken');
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'authToken': token || '',
          'idLista': idLista
        });
        return this.http.post<lista>(`${this.apiUrl}/addProducto`, producto, { headers });
      })
    );
  }

  obtenerProductos(idLista: string): Observable<producto[]> {
    return from(this.userService.checkCookie()).pipe(
      switchMap(() => {
        const token = localStorage.getItem('authToken');
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'authToken': token || '',
          'idLista': idLista
        });
        return this.http.get<producto[]>(`${this.apiUrl}/obtenerProductos`, { headers });
      })
    );
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

  eliminarLista(idLista: string): Observable<any> {
    return from(this.userService.checkCookie()).pipe(
      switchMap(() => {
        const token = localStorage.getItem('authToken');
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'authToken': token || ''
        });
        return this.http.delete<any>(`${this.apiUrl}/eliminarLista/${idLista}`, { headers });
      })
    );
  }

  comprar(idProducto: String, unidadesCompradas: number): Observable<producto> {
    return from(this.userService.checkCookie()).pipe(
      switchMap(() => {
        const token = localStorage.getItem('authToken');
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'authToken': token || ''
        });
        return this.http.put<any>(`${this.apiUrl}/comprar`, {idProducto, unidadesCompradas}, { headers });
      })
    );
  }

  eliminarProducto(idProducto: String): Observable<any> {
    return from(this.userService.checkCookie()).pipe(
      switchMap(() => {
        const token = localStorage.getItem('authToken');
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'authToken': token || ''
        });
        return this.http.delete<any>(`${this.apiUrl}/eliminarProducto/${idProducto}`, { headers });
      })
    );
  }

  addInvitado(idLista: string, email: string): Observable<string> {
    return from(this.userService.checkCookie()).pipe(
      switchMap(() => {
        const token = localStorage.getItem('authToken');
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'authToken': token || '',
          'idLista': idLista
        });
        return this.http.post(`${this.apiUrl}/addInvitado`, email, { headers, responseType: 'text'});
      })
    );
  }

  obtenerInvitados(idLista: string): Observable<EmailUsuario[]> {
    return from(this.userService.checkCookie()).pipe(
      switchMap(() => {
        const token = localStorage.getItem('authToken');
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'authToken': token || '',
          'idLista': idLista
        });
        return this.http.get<EmailUsuario[]>(`${this.apiUrl}/obtenerInvitados`, { headers });
      })
    );
  }

  eliminarInvitado(idLista: string, email: string): Observable<any> {
    return from(this.userService.checkCookie()).pipe(
      switchMap(() => {
        const token = localStorage.getItem('authToken');
        const headers = new HttpHeaders({
          'Content-Type': 'application/json',
          'authToken': token || '',
          'idLista': idLista
        });
        return this.http.delete<any>(`${this.apiUrl}/eliminarInvitado/${email}`, { headers });
      })
    );
  }
}