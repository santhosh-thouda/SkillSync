package com.capgemini.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Security Configuration for the API Gateway.
 * Configures the WebFlux Security chain to disable CSRF (since we use stateless JWTs)
 * and permits all exchanges at the edge, relying on downstream microservices to
 * enforce their own method-level security via JwtAuthenticationFilters.
 */
@Configuration
public class GatewaySecurityConfig {

    /**
     * Builds the SecurityWebFilterChain.
     *
     * @param http The ServerHttpSecurity provided by Spring WebFlux.
     * @return The configured security chain.
     */
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(csrf -> csrf.disable())
            .authorizeExchange(exchange -> exchange
                .anyExchange().permitAll()
            );
        return http.build();
    }
}
