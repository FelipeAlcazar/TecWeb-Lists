package edu.uclm.esi.listasbe.services;
import edu.uclm.esi.listasbe.ws.wsListas;
import java.util.Optional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.listasbe.dao.ListaDao;
import edu.uclm.esi.listasbe.dao.ProductoDao;
import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;

@Service
public class ListaService {
		
	@Autowired
	private ListaDao listaDao;
	
	@Autowired
	private wsListas wsListas;
	
	@Autowired
	private ProxyBEU proxy;
	
	@Autowired
	private ProductoDao productoDao;
	
	/**
	public Lista crearLista(String nombre, String token) {
		boolean correcto = this.proxy.validarToken(token);
		
		if(!correcto)
		throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);
		
		Lista lista= new Lista();
		lista.setNombre(nombre);
		
		
		
		this.listaDao.save(lista);
		return lista;
	}
	*/
	public Lista crearLista(String nombre, String token) {
		String email = this.proxy.validar(token);

		if (email==null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);
		
		Lista lista = new Lista();
		lista.setNombre(nombre);
		lista.addEmailUsuario(email);
		this.listaDao.save(lista);
		this.listaDao.confirmar(lista.getId(), email);
		return lista;
	}

	public Iterable<Lista> obtenerListas(String token) {
		String email = this.proxy.validar(token);

		if (email==null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);
		
		Iterable<String> listas_id = this.listaDao.getListasDe(email);
		Iterable<Lista> listas = this.listaDao.findAllById(listas_id);

		List<Lista> sortedListas = StreamSupport.stream(listas.spliterator(), false)
        .sorted(Comparator.comparing(Lista::getNombre))
        .collect(Collectors.toList());

		return sortedListas;
	}

	public void eliminarLista(String token, String idLista) {
		String email = this.proxy.validar(token);

		if (email==null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);
		
		Optional<Lista> optLista=this.listaDao.findById(idLista);
		
		if(optLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		
		Lista lista=optLista.get();
		if(!lista.getEmailsUsuarios().contains(email))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,"No tienes permisos para eliminar esta lista");
		
		this.listaDao.delete(lista);
	}
	
	public Lista addProducto(String idLista, Producto producto) {
		Optional<Lista> optLista=this.listaDao.findById(idLista);
		
		if(optLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		
		Lista lista=optLista.get();
		lista.add(producto);
		
		producto.setLista(lista);
		this.productoDao.save(producto);
		
		this.wsListas.notificar(idLista,producto);
		return lista;
	}
	
	public String addInvitado(String idLista, String email) {
		Optional<Lista> optLista=this.listaDao.findById(idLista);
		
		if(optLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No se encuentra la lista");
		
		Lista lista=optLista.get();
		lista.addEmailUsuario(email);
		this.listaDao.save(lista);
		
	    String url = "http://localhost:80/listas/aceptarInvitacion?email=" + email + "&lista=" + idLista;
		return url;

	}
	
	public Producto comprar(String idProducto, float unidadesCompradas) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void aceptarInvitacion(String idLista, String email) {
		this.listaDao.confirmar(idLista, email);
	}

}














