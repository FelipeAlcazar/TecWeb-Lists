package edu.uclm.esi.fakeaccountsbe.services;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentUpdateParams;

@Service
public class PagosService {

    static {
        Stripe.apiKey = "sk_test_51Q7a6EP5FVnktAtkExVGrwH7PUZnEK1FOtSwkQ90ZHBZRMpkCT2zUa3AH46exGV0IQZp4DRHL50Jx0yhQyQwldXh00TQBS6gTo";
    }

    public Map<String, String> prepararTransaccion(long importe, String email, String paymentMethodId) {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setCurrency("eur")
                .setAmount(importe)
                .setReceiptEmail(email)
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

            return Map.of(
                "id", intent.getId(),
                "clientSecret", intent.getClientSecret()
            );
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