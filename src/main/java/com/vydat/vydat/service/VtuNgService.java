package com.vydat.vydat.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class VtuNgService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String VTU_LOGIN_URL = "https://vtu.ng/wp-json/jwt-auth/v1/token";
    private static final String VTU_FUND_URL = "https://vtu.ng/wp-json/api/v2/wallet/fund";

    private final String vtuUsername = "ABDULLAHI"; //System.getenv("VTU_USERNAME"); // your VTU username/email
    private final String vtuPassword = "12Aremu!@"; //System.getenv("VTU_PASSWORD"); // your VTU password

    private String jwtToken;

    public void fundVtuWallet(double amountNaira) {
        ensureToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);

        Map<String, Object> body = Map.of("amount", amountNaira);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(VTU_FUND_URL, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to credit VTU wallet: " + response.getBody());
        }
    }

    private void ensureToken() {
        if (jwtToken == null) {
            authenticate();
        }
    }

    private void authenticate() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = Map.of(
                "username", vtuUsername,
                "password", vtuPassword
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(VTU_LOGIN_URL, request, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("VTU NG login failed: " + response.getBody());
        }

        jwtToken = (String) response.getBody().get("token");
    }
}
