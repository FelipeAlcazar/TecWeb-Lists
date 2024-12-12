package edu.uclm.esi.fakeaccountsbe.http;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.fakeaccountsbe.model.User;
import edu.uclm.esi.fakeaccountsbe.services.UserService;

@RestController
@RequestMapping("tokens")
@CrossOrigin("*")
public class TokenController {

    @Autowired
    private UserService userService;

    @PutMapping("/validar")
    public String validar(@RequestBody String token) {
        if(new Random().nextBoolean()) {
        	//La validación puede ir según el pago CUIDADO, esta puesto random
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED);
        }
        User user = userService.findByToken(token);
        return user.getEmail();
    }
}