package com.vydat.vydat.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;

@Service
public class VtuClient {

    @Value("${vtu.username}")
    private String username;

    @Value("${vtu.password}")
    private String password;

    @Value("${vtu.base.url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Fetch a fresh JWT token before each request
    private String getToken() {
        String url = "https://vtu.ng/wp-json/jwt-auth/v1/token";
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        return (String) response.get("token");
    }

    private HttpEntity<Map<String, Object>> buildRequest(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getToken());
        return new HttpEntity<>(body, headers);
    }

    public Map<String, Object> buyAirtime(String network, String phone, double amount, String ref) {
        Map<String, Object> req = new HashMap<>();
        req.put("network", network);
        req.put("phone", phone);
        req.put("amount", amount);
        req.put("request_id", ref);
        return restTemplate.postForObject(baseUrl + "/airtime", buildRequest(req), Map.class);
    }

    public Map<String, Object> buyData(String network, String phone, String plan, String ref) {
        Map<String, Object> req = new HashMap<>();
        req.put("network", network);
        req.put("phone", phone);
        req.put("plan", plan);
        req.put("request_id", ref);
        return restTemplate.postForObject(baseUrl + "/data", buildRequest(req), Map.class);
    }

    public Map<String, Object> payCable(String provider, String smartcard, String plan, String ref) {
        Map<String, Object> req = new HashMap<>();
        req.put("provider", provider);
        req.put("smartcard_number", smartcard);
        req.put("plan", plan);
        req.put("request_id", ref);
        return restTemplate.postForObject(baseUrl + "/cable", buildRequest(req), Map.class);
    }

    public Map<String, Object> payElectricity(String disco, String meter, String meterType, double amount, String ref) {
        Map<String, Object> req = new HashMap<>();
        req.put("disco", disco);
        req.put("meter", meter);
        req.put("meter_type", meterType);
        req.put("amount", amount);
        req.put("request_id", ref);
        return restTemplate.postForObject(baseUrl + "/electricity", buildRequest(req), Map.class);
    }

    public Map<String, Object> buyExamPin(String examType, int quantity, String ref) {
        Map<String, Object> req = new HashMap<>();
        req.put("exam_type", examType);
        req.put("quantity", quantity);
        req.put("request_id", ref);
        return restTemplate.postForObject(baseUrl + "/exam", buildRequest(req), Map.class);
    }

    public Map<String, Object> getBalance() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getToken());
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(baseUrl + "/balance", HttpMethod.GET, request, Map.class).getBody();
    }
}