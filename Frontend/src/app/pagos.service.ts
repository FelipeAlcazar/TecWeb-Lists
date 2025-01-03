import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { catchError, tap } from 'rxjs/operators';
import { throwError } from 'rxjs/internal/observable/throwError';

@Injectable({
  providedIn: 'root'
})
export class PagosService {
  private apiUrl = "http://localhost:9000/pagos";

  constructor(private http: HttpClient) { }

  prepararTransaccion(importe: number, token: string, paymentMethodId: string) {
    const url = `${this.apiUrl}/prepararTransaccion`;
    return this.http.put<{ clientSecret: string }>(url, { importe, token, paymentMethodId }, { observe: 'response' }).pipe(
      catchError((error) => {
        console.error('Prepare transaction error:', error);
        return throwError(error);
      })
    );
  }
}