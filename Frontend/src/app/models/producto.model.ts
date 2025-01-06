export class producto{
    id:string
    nombre:string
    unidadesPedidas:number
    unidadesCompradas:number

    constructor(){
        this.id = "";
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