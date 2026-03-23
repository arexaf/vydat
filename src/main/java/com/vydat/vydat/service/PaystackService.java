package com.vydat.vydat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PaystackService {

    private final RestTemplate restTemplate;

    @Value("${paystack.secret.key}")
    private String paystackSecret;

    public PaystackService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(paystackSecret);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // Step 1: Create Paystack customer with full details
    public String createCustomer(String email, String firstName, String lastName, String phone) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("first_name", firstName);
        body.put("last_name", lastName);
        body.put("phone", phone);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.paystack.co/customer", entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return (String) data.get("customer_code");
            } else {
                throw new RuntimeException("Failed to create customer: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating Paystack customer: " + e.getMessage(), e);
        }
    }

    // Step 2: Create dedicated virtual account
    public String createVirtualAccount(String customerCode) {
        Map<String, Object> body = new HashMap<>();
        body.put("customer", customerCode);
        body.put("preferred_bank", "wema-bank");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, buildHeaders());

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.paystack.co/dedicated_account", entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                return (String) data.get("account_number");
            } else {
                throw new RuntimeException("Failed to create virtual account: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error creating Paystack virtual account: " + e.getMessage(), e);
        }
    }

    // Combined: splits username into first/last name then creates customer + DVA
    public String createCustomerAndVirtualAccount(String email, String username, String phone) {
        String[] parts = username.trim().split("\\s+", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : parts[0];

        String customerCode = createCustomer(email, firstName, lastName, phone);
        return createVirtualAccount(customerCode);
    }
}