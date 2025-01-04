package edu.uclm.esi.fakeaccountsbe.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.fakeaccountsbe.model.User;
import edu.uclm.esi.fakeaccountsbe.services.PagosService;
import edu.uclm.esi.fakeaccountsbe.services.UserService;

import java.util.Map;

@RestController
@RequestMapping("pagos")
public class PagosController {

    @Autowired
    private PagosService pagosService;

    @Autowired
    private UserService userService;

    @PutMapping("/prepararTransaccion")
    public Map<String, String> prepararTransaccion(@RequestBody Map<String, Object> request) {
        float importe = ((Number) request.get("importe")).floatValue();
        String token = (String) request.get("token");
        String paymentMethodId = (String) request.get("paymentMethodId");

        // Find the user by token
        User user = userService.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Map<String, String> paymentIntentData = this.pagosService.prepararTransaccion((long) (importe * 100), user.getEmail(), paymentMethodId);

        // Confirm the PaymentIntent and update the user's hasPaid status
        boolean paymentSuccess = this.pagosService.confirmPayment(paymentIntentData.get("id"));
        if (paymentSuccess) {
            user.setHasPaid(true);
            userService.save(user);
        }

        return paymentIntentData;
    }
}