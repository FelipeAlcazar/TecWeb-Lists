import { Injectable } from '@angular/core';
import { lista } from './models/lista.model';

@Injectable({
  providedIn: 'root'
})
export class ManagerService {
  listaSeleccionada? : lista;
  constructor() { }
}
