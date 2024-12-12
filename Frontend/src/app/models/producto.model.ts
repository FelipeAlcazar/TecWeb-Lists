export class producto{
    nombre:string
    unidadesPedidas:number
    unidadesCompradas:number

    constructor(){
        this.nombre="";
        this.unidadesPedidas=0;
        this.unidadesCompradas=0;
    }

    crearProducto(nombre:string, unidadesPedidas:number, unidadesCompradas:number){
        this.nombre=nombre;
        this.unidadesPedidas=unidadesPedidas;
        this.unidadesCompradas=unidadesCompradas;
    }
}