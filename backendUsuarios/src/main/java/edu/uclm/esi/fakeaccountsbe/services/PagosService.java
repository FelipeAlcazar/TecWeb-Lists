package edu.uclm.esi.fakeaccountsbe.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentUpdateParams;

import edu.uclm.esi.fakeaccountsbe.model.User;

@Service
public class PagosService {

    static {
        Stripe.apiKey = "sk_test_51Q7a6EP5FVnktAtkExVGrwH7PUZnEK1FOtSwkQ90ZHBZRMpkCT2zUa3AH46exGV0IQZp4DRHL50Jx0yhQyQwldXh00TQBS6gTo";
    }

    @Autowired
    private UserService userService;

    public Map<String, String> prepararTransaccion(Map<String, Object> request) {
        float importe = ((Number) request.get("importe")).floatValue();
        String token = (String) request.get("token");
        String paymentMethodId = (String) request.get("paymentMethodId");

        // Find the user by token
        User user = userService.findByToken(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setCurrency("eur")
                .setAmount((long) (importe * 100))
                .setReceiptEmail(user.getEmail())
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                        .build()
                )
                .build();

        PaymentIntent intent;
        try {
            intent = PaymentIntent.create(params);

            // Attach the payment method to the PaymentIntent
            PaymentIntentUpdateParams updateParams = PaymentIntentUpdateParams.builder()
                    .setPaymentMethod(paymentMethodId)
                    .build();
            intent = intent.update(updateParams);

            Map<String, String> paymentIntentData = Map.of(
                "id", intent.getId(),
                "clientSecret", intent.getClientSecret()
            );

            // Confirm the PaymentIntent and update the user's hasPaid status
            boolean paymentSuccess = this.confirmPayment(paymentIntentData.get("id"));
            if (paymentSuccess) {
                user.setHasPaid(true);
                userService.save(user);
            }

            return paymentIntentData;
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to create or update PaymentIntent", e);
        }
    }

    public boolean confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            paymentIntent = paymentIntent.confirm();
            return "succeeded".equals(paymentIntent.getStatus());
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to confirm PaymentIntent", e);
        }
    }
}