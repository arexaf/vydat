package com.vydat.vydat.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PaystackService {
    private final RestTemplate restTemplate;

    // ✅ Make sure this is your TEST or LIVE SECRET KEY from Paystack dashboard
    private static final String PAYSTACK_SECRET = "";

    public PaystackService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Step 1: Create Paystack customer
    public String createCustomer(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(PAYSTACK_SECRET);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("email", email);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = "https://api.paystack.co/customer";

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return (String) data.get("customer_code"); // ✅ return customer_code
            } else {
                throw new RuntimeException("Failed to create Paystack customer. Status: "
                        + response.getStatusCode() + ", body: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating Paystack customer: " + e.getMessage(), e);
        }
    }

    // Step 2: Create dedicated virtual account (DVA)
    public String createVirtualAccount(String customerCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(PAYSTACK_SECRET);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "customer", customerCode,
                "preferred_bank", "wema-bank" // optional, can remove to let Paystack choose
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String url = "https://api.paystack.co/dedicated_account";

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return (String) data.get("account_number");
            } else {
                throw new RuntimeException("Failed to create Paystack virtual account. Status: "
                        + response.getStatusCode() + ", body: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating Paystack virtual account: " + e.getMessage(), e);
        }
    }
}
