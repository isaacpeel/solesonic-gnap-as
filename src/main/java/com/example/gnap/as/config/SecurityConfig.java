package com.example.gnap.as.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the GNAP Authorization Server.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure security filter chain.
     *
     * @param http the HTTP security
     * @return the security filter chain
     * @throws Exception if an error occurs
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/gnap/grant", "/gnap/token/**"))
            .authorizeHttpRequests(authorize -> authorize
                // Public endpoints
                .requestMatchers("/gnap/grant").permitAll()
                .requestMatchers("/gnap/grant/*").permitAll()
                .requestMatchers("/gnap/token/**").permitAll()
                .requestMatchers("/gnap/interact/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives("frame-ancestors 'self'"))
            ); // For H2 console

        return http.build();
    }
}
