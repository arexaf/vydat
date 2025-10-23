package com.vydat.vydat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // disable CSRF for API
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() // ✅ allow everything for now
            );

        return http.build();
    }

    // ✅ Add RestTemplate bean here
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
