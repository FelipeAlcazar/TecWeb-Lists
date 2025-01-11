package edu.uclm.esi.fakeaccountsbe.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.esi.fakeaccountsbe.services.TokenService;


@RestController
@RequestMapping("tokens")
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @PutMapping("/validar")
    public String validar(@RequestBody String token) {
        return tokenService.validar(token);
    }
}