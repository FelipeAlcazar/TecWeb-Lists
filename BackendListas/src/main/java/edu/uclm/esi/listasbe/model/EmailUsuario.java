package edu.uclm.esi.listasbe.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class EmailUsuario {
    private String email;
    private boolean propietario;
    private boolean confirmado;

    public EmailUsuario() {}

    public EmailUsuario(String email, boolean propietario, boolean confirmado) {
        this.email = email;
        this.propietario = propietario;
        this.confirmado = confirmado;
    }

    // Getters y setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isPropietario() {
        return propietario;
    }

    public void setPropietario(boolean propietario) {
        this.propietario = propietario;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }
}