package edu.uclm.esi.fakeaccountsbe.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.fakeaccountsbe.services.PagosService;

import java.util.Map;

@RestController
@RequestMapping("pagos")
public class PagosController {

    @Autowired
    private PagosService pagosService;

    @PutMapping("/prepararTransaccion")
    public Map<String, String> prepararTransaccion(@RequestBody Map<String, Object> request) {
        return pagosService.prepararTransaccion(request);
    }
}