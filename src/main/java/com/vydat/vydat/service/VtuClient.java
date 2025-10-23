package com.vydat.vydat.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Service
public class VtuClient {

    @Value("${vtu.api.key}")
    private String apiKey;

    @Value("${vtu.base.url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> buyAirtime(String network, String phone, double amount, String ref) {
        String url = baseUrl + "/airtime";
        Map<String, Object> req = new HashMap<>();
        req.put("api_key", apiKey);
        req.put("network", network);
        req.put("phone", phone);
        req.put("amount", amount);
        req.put("request_id", ref);

        return restTemplate.postForObject(url, req, Map.class);
    }

    public Map<String, Object> buyData(String network, String phone, String plan, String ref) {
        String url = baseUrl + "/data";
        Map<String, Object> req = new HashMap<>();
        req.put("api_key", apiKey);
        req.put("network", network);
        req.put("phone", phone);
        req.put("plan", plan);
        req.put("request_id", ref);

        return restTemplate.postForObject(url, req, Map.class);
    }

    public Map<String, Object> payCable(String provider, String smartcard, String plan, String ref) {
        String url = baseUrl + "/cable";
        Map<String, Object> req = new HashMap<>();
        req.put("api_key", apiKey);
        req.put("provider", provider);
        req.put("smartcard_number", smartcard);
        req.put("plan", plan);
        req.put("request_id", ref);

        return restTemplate.postForObject(url, req, Map.class);
    }

    public Map<String, Object> payElectricity(String disco, String meter, String meterType, double amount, String ref) {
        String url = baseUrl + "/electricity";
        Map<String, Object> req = new HashMap<>();
        req.put("api_key", apiKey);
        req.put("disco", disco);
        req.put("meter", meter);
        req.put("meter_type", meterType);
        req.put("amount", amount);
        req.put("request_id", ref);

        return restTemplate.postForObject(url, req, Map.class);
    }

    public Map<String, Object> buyExamPin(String examType, int quantity, String ref) {
        String url = baseUrl + "/exam";
        Map<String, Object> req = new HashMap<>();
        req.put("api_key", apiKey);
        req.put("exam_type", examType); // e.g. WAEC, NECO, JAMB
        req.put("quantity", quantity);
        req.put("request_id", ref);

        return restTemplate.postForObject(url, req, Map.class);
    }

    public Map<String, Object> getBalance() {
        String url = baseUrl + "/balance?api_key=" + apiKey;
        return restTemplate.getForObject(url, Map.class);
    }
}
