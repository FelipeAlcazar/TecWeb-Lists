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
import edu.uclm.esi.listasbe.model.EmailUsuario;
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
	 * public Lista crearLista(String nombre, String token) {
	 * boolean correcto = this.proxy.validarToken(token);
	 * 
	 * if(!correcto)
	 * throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);
	 * 
	 * Lista lista= new Lista();
	 * lista.setNombre(nombre);
	 * 
	 * 
	 * 
	 * this.listaDao.save(lista);
	 * return lista;
	 * }
	 */
	public Lista crearLista(String nombre, String token) {
		String email = this.proxy.validar(token);

		if (email == null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);

		// Check if the user has paid
		boolean hasPaid = this.proxy.hasPaid(token);

		// Check if the user has already created 2 lists if they haven't paid
		if (!hasPaid) {
			int listCount = this.listaDao.countByEmailsUsuarios_Email(email);
			if (listCount >= 2) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN,
						"Los usuarios no registrados o no han pagado solo pueden crear 2 listas.");
			}
		}
		Lista lista = new Lista();
		lista.setNombre(nombre);
		lista.addEmailUsuario(new EmailUsuario(email, true, true)); // Add owner to the list of users
		this.listaDao.save(lista);
		return lista;
	}

	public Iterable<Lista> obtenerListas(String token) {
		String email = this.proxy.validar(token);

		if (email == null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);

		Iterable<String> listas_id = this.listaDao.getListasDe(email);
		Iterable<Lista> listas = this.listaDao.findAllById(listas_id);

		List<Lista> sortedListas = StreamSupport.stream(listas.spliterator(), false)
				.sorted(Comparator.comparing(Lista::getNombre))
				.collect(Collectors.toList());

		return sortedListas;
	}

	public Iterable<Producto> obtenerProductos(String token, String idLista) {
		String email = this.proxy.validar(token);

		if (email == null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);
		Optional<Lista> optLista = this.listaDao.findById(idLista);

		if (optLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");

		Lista lista = optLista.get();
		if (!lista.getEmailsUsuarios().contains(email))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para ver esta lista");

		return lista.getProductos();
	}

	public void eliminarLista(String token, String idLista) {
		String email = this.proxy.validar(token);

		if (email == null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);

		Optional<Lista> optLista = this.listaDao.findById(idLista);

		if (optLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");

		Lista lista = optLista.get();
		boolean isOwner = lista.getUsuarios().stream()
				.anyMatch(usuario -> usuario.getEmail().equals(email) && usuario.isPropietario());

		if (!isOwner)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el propietario puede eliminar esta lista");

		this.listaDao.delete(lista);
	}

	public Lista addProducto(String token, String idLista, Producto producto) {
		String email = this.proxy.validar(token);

		if (email == null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);

		Optional<Lista> optLista = this.listaDao.findById(idLista);

		if (optLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");

		Lista lista = optLista.get();
		boolean hasPaid = this.proxy.hasPaid(token);
		if (!hasPaid) {
			int productCount = lista.getProductos().size();
			if (productCount >= 10) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN,
						"Los usuarios no registrados o no han pagado solo pueden crear 2 listas.");
			}
		}
		Producto productoAux = new Producto();
		productoAux.setNombre(producto.getNombre());
		productoAux.setUnidadesPedidas(producto.getUnidadesPedidas());
		productoAux.setUnidadesCompradas(0);
		lista.add(productoAux);

		productoAux.setLista(lista);
		this.productoDao.save(productoAux);

		this.wsListas.notificar(idLista, producto);
		return lista;
	}

	public String addInvitado(String idLista, String email) {
		Optional<Lista> optLista = this.listaDao.findById(idLista);

		if (optLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");

		Lista lista = optLista.get();
		boolean hasPaid = this.proxy.hasPaid(email);
		if (!hasPaid) {
			int userCount = lista.getUsuarios().size();
			if (userCount >= 2) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN,
						"Los usuarios que no han pagado solo pueden crear 2 listas.");
			}
		}
		if (lista.getEmailsUsuarios().contains(email))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email ya está en la lista");

		EmailUsuario usuario = new EmailUsuario(email, false, false);
		lista.addEmailUsuario(usuario);
		this.listaDao.save(lista);

		String url = "http://localhost:80/listas/aceptarInvitacion?email=" + email + "&idlista=" + idLista;
		return url;

	}

	public Iterable<EmailUsuario> obtenerInvitados(String token, String idLista){
		String email = this.proxy.validar(token);

		if (email == null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);

		Optional<Lista> optLista = this.listaDao.findById(idLista);

		if (optLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");

		Lista lista = optLista.get();
		return lista.getUsuarios();
	}

	public void eliminarInvitado(String token, String idLista, String email){
		String emailPropietario = this.proxy.validar(token);

		if (emailPropietario == null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);

		Optional<Lista> optLista = this.listaDao.findById(idLista);

		if (optLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");

		Lista lista = optLista.get();
		boolean isOwner = lista.getUsuarios().stream()
				.anyMatch(usuario -> usuario.getEmail().equals(emailPropietario) && usuario.isPropietario());

		if (!isOwner)
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el propietario puede eliminar invitados");

		List<EmailUsuario> emailsUsuarios = lista.getUsuarios();
		EmailUsuario emailUsuario = emailsUsuarios.stream()
				.filter(usuario -> usuario.getEmail().equals(email))
				.findFirst()
				.orElse(null);

		if (emailUsuario == null)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra el email en la lista");

		emailsUsuarios.remove(emailUsuario);
		lista.setEmailsUsuarios(emailsUsuarios);
		this.listaDao.save(lista);
	}

	public Producto comprar(String token, String idProducto, float unidadesCompradas) {
		String email = this.proxy.validar(token);

		if (email == null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);

		Optional<Producto> optProducto = this.productoDao.findById(idProducto);

		if (optProducto.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra el producto");

		Producto producto = optProducto.get();
		float unidadesCompradasTotales = producto.getUnidadesCompradas() + unidadesCompradas;

		if (unidadesCompradasTotales > producto.getUnidadesPedidas())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay suficientes unidades");

		Lista lista = producto.getLista();

		if (!lista.getEmailsUsuarios().contains(email))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para comprar este producto");

		this.productoDao.comprar(idProducto, unidadesCompradasTotales);
		producto.setUnidadesCompradas(unidadesCompradasTotales);

		// Notify WebSocket clients about the update
		this.wsListas.notificar(lista.getId(), producto);

		return producto;
	}

	public void eliminarProducto(String token, String idProducto) {
		String email = this.proxy.validar(token);

		if (email == null)
			throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);

		Optional<Producto> optProducto = this.productoDao.findById(idProducto);

		if (optProducto.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra el producto");

		Producto producto = optProducto.get();
		Lista lista = producto.getLista();

		if (!lista.getEmailsUsuarios().contains(email))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permisos para eliminar este producto");

		this.productoDao.delete(producto);
		lista.remove(producto);
	}

	public void aceptarInvitacion(String idLista, String email) {
		Optional<Lista> optLista = this.listaDao.findById(idLista);

		if (optLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encuentra la lista");

		Lista lista = optLista.get();
		List<EmailUsuario> emailsUsuarios = lista.getUsuarios();
		for (EmailUsuario emailUsuario : emailsUsuarios) {
			if (emailUsuario.getEmail().equals(email)) {
				emailUsuario.setConfirmado(true);
				break;
			}
		}

		this.listaDao.save(lista);

	}

}
