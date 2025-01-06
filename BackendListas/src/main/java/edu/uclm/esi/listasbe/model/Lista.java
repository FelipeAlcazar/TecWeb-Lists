package edu.uclm.esi.listasbe.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Lista {
	@Id @Column(length=36)
	private String id;
	
	@Column(length=80)
	private String nombre;
	
	@OneToMany(mappedBy = "lista")
	private List<Producto> productos;
	
	@ElementCollection
	private List<EmailUsuario> emailsUsuarios;
	
	public Lista() {
		this.id=UUID.randomUUID().toString();
		this.productos=new ArrayList<>();
		this.emailsUsuarios=new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Producto> getProductos() {
		return productos;
	}

	public void setProductos(List<Producto> productos) {
		this.productos = productos;
	}
	

	public List<String> getEmailsUsuarios() {
		List<String> emails = new ArrayList<>();
		for (EmailUsuario emailUsuario : this.emailsUsuarios) {
			emails.add(emailUsuario.getEmail());
		}
		return emails;
	}

	public List<EmailUsuario> getUsuarios() {
		return this.emailsUsuarios;
	}

	public void setEmailsUsuarios(List<EmailUsuario> emailsUsuarios) {
		this.emailsUsuarios = emailsUsuarios;
	}

	public void add(Producto producto) {
		this.productos.add(producto);
	}

	public void remove(Producto producto) {
		this.productos.remove(producto);
	}
    public void addEmailUsuario(EmailUsuario usuario) {
        if (usuario == null)
            throw new IllegalArgumentException("El email no puede estar vacío");
        this.emailsUsuarios.add(usuario);
    }
	
}
