package com.vydat.vydat.service;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class VtuWalletService {
    private final VtuAuthService vtuAuthService;
    private final RestTemplate restTemplate;
    

    public VtuWalletService(VtuAuthService vtuAuthService, RestTemplate restTemplate) {
        this.vtuAuthService = vtuAuthService;
        this.restTemplate = restTemplate;
    }

    public String getBalance(){
        String token = vtuAuthService.getToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String url = "https://vtu.ng/wp-json/api/v2/balance";
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody().toString();
        } else {
            throw new RuntimeException("Failed to get balance.");
        }
    }

}
