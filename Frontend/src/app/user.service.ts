import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse, HttpHeaders } from '@angular/common/http';
import { catchError, tap, concatMap } from 'rxjs/operators';
import { throwError, from } from 'rxjs';

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
          localStorage.setItem('userEmail', email); // Store email
        }
      })
    );
  }

  sendConfirmationEmail(email: string) {
    const urlSendConfirmationEmail = `${this.apiUrl}/sendConfirmationEmail`;
    return this.http.post<{ message: string }>(urlSendConfirmationEmail, { email: email }, { observe: 'response', withCredentials: true }).pipe(
      tap((response: HttpResponse<{ message: string }>) => {
        console.log(response.body?.message);
      }),
      catchError((error) => {
        console.error('Send confirmation email error:', error);
        return throwError(error);
      })
    );
  }

  confirmEmail(token: string) {
    const urlConfirmEmail = `${this.apiUrl}/confirmEmail`;
    return this.http.post<{ message: string }>(urlConfirmEmail, { token: token }, { observe: 'response', withCredentials: true }).pipe(
      tap((response: HttpResponse<{ message: string }>) => {
        console.log(response.body?.message);
      }),
      catchError((error) => {
        console.error('Confirm email error:', error);
        return throwError(error);
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
          localStorage.setItem('userEmail', email); // Store email
        }
      }),
      catchError((error) => {
        console.error('Login error:', error);
        return throwError(error);
      })
    );
  }

  checkCookie(): Promise<void> {
    const urlCheckCookie = `${this.apiUrl}/checkCookie`;
    return new Promise((resolve, reject) => {
      this.http.get<{ token: string } | null>(urlCheckCookie, { observe: 'response', withCredentials: true }).pipe(
        tap((response: HttpResponse<{ token: string } | null>) => {
          const token = response.body?.token;
          if (token) {
            localStorage.setItem('authToken', token);
            resolve();
          } else {
            reject('No token found');
          }
        }),
        catchError((error) => {
          console.error('Check cookie error:', error);
          reject(error);
          return throwError(error);
        })
      ).subscribe();
    });
  }

  getUserHasPaid() {
    return from(this.checkCookie()).pipe(
      concatMap(() => {
        const token = localStorage.getItem('authToken');
        let urlGetUserStatus = this.apiUrl + '/haspaid';
        return this.http.get<{ hasPaid: boolean }>(urlGetUserStatus, { 
          observe: 'response', 
          headers: new HttpHeaders({ 'token': token || '' }),
          withCredentials: true 
        }).pipe(
          catchError((error) => {
            console.error('Get user status error:', error);
            return throwError(error);
          })
        );
      })
    );
  }

  logout() {
    return from(this.checkCookie()).pipe(
      concatMap(() => {
        let urlLogout = this.apiUrl + '/logout';
        return this.http.post(urlLogout, {}, { withCredentials: true }).pipe(
          tap(() => {
            // Optionally, clear any client-side cookies here
            localStorage.removeItem('authToken');
            localStorage.removeItem('userEmail'); // Clear email
          }),
          catchError((error) => {
            console.error('Logout error:', error);
            return throwError(error);
          })
        );
      })
    );
  }

  getCurrentUserEmail(): string | null {
    return localStorage.getItem('userEmail');
  }
}