package edu.uclm.esi.fakeaccountsbe.services;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
@Service
public class PagosService {
	
	static {
		Stripe.apiKey="sk_test_51Q7a6EP5FVnktAtkExVGrwH7PUZnEK1FOtSwkQ90ZHBZRMpkCT2zUa3AH46exGV0IQZp4DRHL50Jx0yhQyQwldXh00TQBS6gTo";
	}
	
	public String prepararTransaccion(long importe) {
		PaymentIntentCreateParams params = new PaymentIntentCreateParams.Builder()
				.setCurrency("eur")
				.setAmount(importe)
				.build();
		
		PaymentIntent intent;
		try {
			intent=PaymentIntent.create(params);
			JSONObject jso = new JSONObject(intent.toJson());
			String clientSecret = jso.getString("client_secret");
			return clientSecret;
		}catch(StripeException e){
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}

	}
}
