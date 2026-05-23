package com.capgemini.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String secret;

    // Extracts claims from the token and return the principal
    public JwtPrincipal extractPrincipal(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Object userId = claims.get("userId");

        // Check if the userId is Number or a String to convert it to Long
        Long parsedUserId = userId instanceof Number number ? number.longValue() : Long.parseLong(userId.toString());

        return new JwtPrincipal(
                parsedUserId,
                claims.getSubject(),
                claims.get("role", String.class)
        );
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
