import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';
import { throwError } from 'rxjs/internal/observable/throwError';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = "http://localhost:9000/users";
  
  constructor(private http: HttpClient) { }

  register1(email: string, pw1: string, pw2: string) {
    let infoJSN = { email: email, pwd1: pw1, pwd2: pw2 };
    let urlRegister1 = this.apiUrl + "/registrar1";
    return this.http.post<{ token: string }>(urlRegister1, infoJSN, { observe: 'response', withCredentials: true }).pipe(
      tap((response: HttpResponse<{ token: string }>) => {
        const token = response.body?.token;
        if (token) {
          localStorage.setItem('authToken', token);
        }
      })
    );
  }

  login1(email: string, pwd: string) {
    let info = { email: email, pwd: pwd };
    let urlLogin1 = this.apiUrl + '/login1';
    return this.http.put<{ token: string }>(urlLogin1, info, { observe: 'response', withCredentials: true }).pipe(
      tap((response: HttpResponse<{ token: string }>) => {
        const token = response.body?.token;
        if (token) {
          localStorage.setItem('authToken', token);
        }
      }),
      catchError((error) => {
        console.error('Login error:', error);
        return throwError(error);
      })
    );
  }

  checkCookie() {
    let urlCheckCookie = this.apiUrl + '/checkCookie';
    return this.http.get<{ token: string } | null>(urlCheckCookie, { observe: 'response', withCredentials: true }).pipe(
      tap((response: HttpResponse<{ token: string } | null>) => {
        const token = response.body?.token;
        if (token) {
          localStorage.setItem('authToken', token);
        }
      }),
      catchError((error) => {
        console.error('Check cookie error:', error);
        return throwError(error);
      })
    );
  }

  logout() {
    let urlLogout = this.apiUrl + '/logout';
    return this.http.post(urlLogout, {}, { withCredentials: true }).pipe(
      tap(() => {
        // Optionally, clear any client-side cookies here
      }),
      catchError((error) => {
        console.error('Logout error:', error);
        return throwError(error);
      })
    );
  }
}