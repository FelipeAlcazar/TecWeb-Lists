package edu.uclm.esi.fakeaccountsbe.http;

import java.util.Collection;

import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
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

import edu.uclm.esi.fakeaccountsbe.model.CredencialesRegistro;
import edu.uclm.esi.fakeaccountsbe.model.User;
import edu.uclm.esi.fakeaccountsbe.services.PagosService;
import edu.uclm.esi.fakeaccountsbe.services.UserService;

@RestController
@RequestMapping("pagos")
public class PagosController {
	@Autowired
	private PagosService pagosService;
	
	@PutMapping("/prepararTransaccion")
	public String prepararTransaccion(@RequestBody float importe) {
		return this.pagosService.prepararTransaccion((long)(importe*100));
		
	}
	
}
















