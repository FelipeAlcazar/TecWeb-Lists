package edu.uclm.esi.listasbe.http;

import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.services.ListaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("listas")
public class ListaController {
	@Autowired
	private ListaService listaService;
	
	@PostMapping("/crearLista")
	public Lista crearLista(HttpServletRequest request, @RequestBody String nombre) {
		nombre = nombre.trim();
		if (nombre.isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede estar vacío");
	
		if (nombre.length() > 80)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la lista está limitado a 80 carácteres");
	
		String token = request.getHeader("authToken");
		if (token == null || token.isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token no puede estar vacío");
	
		return this.listaService.crearLista(nombre, token);
	}

	@GetMapping("/obtenerListas")
	public Iterable<Lista> obtenerListas(HttpServletRequest request){
		String token = request.getHeader("authToken");
		if (token == null || token.isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token no puede estar vacío");
		
		return this.listaService.obtenerListas(token);
	}

	@DeleteMapping("/eliminarLista/{idLista}")
	public void eliminarLista(HttpServletRequest request, @PathVariable String idLista) {
		String token = request.getHeader("authToken");
		if (token == null || token.isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token no puede estar vacío");

		this.listaService.eliminarLista(token, idLista);
	}
	
	
	@PostMapping("/addProducto")
	public Lista addProducto(HttpServletRequest request, @RequestBody Producto producto) {
		if (producto.getNombre().isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El nombre no puede estar vacío");
		
		if (producto.getNombre().length()>80)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"El nombre del producto está limitado a 80 carácteres");

		String idLista=request.getHeader("idLista"); //Nos deben pasar el idLista en la petición (en el postman está en los headers)
		
		return this.listaService.addProducto(idLista, producto);
	}

	@GetMapping("/obtenerProductos")
	public Iterable<Producto> obtenerProductos(HttpServletRequest request){
		String idLista = request.getHeader("idLista");
		if (idLista == null || idLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La idLista no puede estar vacío");
		String token = request.getHeader("authToken");
		if (token == null || token.isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token no puede estar vacío");
		
		return this.listaService.obtenerProductos(token, idLista);
	}


	@PutMapping("/comprar")
	public Producto comprar(HttpServletRequest request, @RequestBody Map<String, Object> compra) {
		String token = request.getHeader("authToken");
		if (token == null || token.isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token no puede estar vacío");
		String idProducto=compra.get("idProducto").toString();
		float unidadesCompradas=Float.parseFloat(compra.get("unidadesCompradas").toString());
		
		return this.listaService.comprar(token, idProducto, unidadesCompradas);
	}

	@DeleteMapping("/eliminarProducto/{idProducto}")
	public void eliminarProducto(HttpServletRequest request, @PathVariable String idProducto) {
		String token = request.getHeader("authToken");
		if (token == null || token.isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El token no puede estar vacío");

		this.listaService.eliminarProducto(token, idProducto);
	}
	
	@PostMapping("/addInvitado")
	public String addInvitado(HttpServletRequest request, @RequestBody String email) {
	    if (email.isEmpty())
	        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El email no puede estar vacío");
	    
	    String idLista = request.getHeader("idLista"); // Nos deben pasar el idLista en la petición (en el postman está en los headers)
	    
	    return this.listaService.addInvitado(idLista, email);
	}
	
	//Hacerlo con el copilot
	@PostMapping("/aceptarInvitacion")
	public void aceptarInvitacion(HttpServletResponse response, @RequestBody String email) {
		//Ella lo hace de esta forma, hay que crear el sendRedirect
		//this.listaService.sendRedirect("http://localhost:6200");
	}
}
















