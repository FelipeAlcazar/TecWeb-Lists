package edu.uclm.esi.listasbe.services;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class ProxyBEU {
    public String validar(String token) {
        String url = "http://localhost:9000/tokens/validar";
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(new StringEntity(token)); // Send the token without extra quotes
            httpPut.setHeader("Content-Type", "application/json");
            HttpContext context = new BasicHttpContext();

            System.out.println("Sending token: " + token);

            try (CloseableHttpResponse response = httpClient.execute(httpPut, context)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getCode() == 200) {
                    return responseBody;
                } else {
                    System.out.println("Error In ProxyBEU: " + responseBody);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return null;
    }

    public boolean hasPaid(String token) {
        String url = "http://localhost:9000/users/haspaid";
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("token", token); // Set the correct header name
            httpGet.setHeader("Content-Type", "application/json");
            HttpContext context = new BasicHttpContext();

            System.out.println("Checking payment status for token: " + token);

            try (CloseableHttpResponse response = httpClient.execute(httpGet, context)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getCode() == 200) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    return jsonResponse.getBoolean("hasPaid");
                } else {
                    System.out.println("Error In ProxyBEU: " + responseBody);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return false;
    }
}