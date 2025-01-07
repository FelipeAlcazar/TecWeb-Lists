export class EmailUsuario {
    email: string;
    propietario: boolean;
    confirmado: boolean;
  
    constructor() {
        this.email= '';
        this.propietario = false;
        this.confirmado = false;
    }
}