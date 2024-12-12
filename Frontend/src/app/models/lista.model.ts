import { producto } from "./producto.model";

export class lista{
    nombre: string;
    id: string;
    productos: producto[];
    constructor(){
        this.nombre = "";
        this.id = "";
        this.productos = [];
    }
}