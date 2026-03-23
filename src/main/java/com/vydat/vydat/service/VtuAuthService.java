package com.vydat.vydat.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vydat.vydat.service.dto.VtuAuthResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class VtuAuthService {

    private final RestTemplate restTemplate;
    private final String username;
    private final String password;
    private final String baseUrl;

    private String cachedToken; // store JWT token temporarily

    public VtuAuthService(RestTemplate restTemplate,
                          @Value("${vtu.api.username:${vtu.username}}") String username,
                          @Value("${vtu.api.password:${vtu.password}}") String password,
                          @Value("${vtu.api.base-url:${vtu.base.url}}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.username = username;
        this.password = password;
        this.baseUrl = baseUrl;
    }

    public String getToken() {
        if (cachedToken == null) {
            cachedToken = authenticate();
        }
        return cachedToken;
    }

    private String authenticate() {
        String url = baseUrl + "/jwt-auth/v1/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<VtuAuthResponse> response =
                restTemplate.postForEntity(url, request, VtuAuthResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().getToken();
        } else {
            throw new RuntimeException("Failed to authenticate with VTU.ng");
        }
    }
}
