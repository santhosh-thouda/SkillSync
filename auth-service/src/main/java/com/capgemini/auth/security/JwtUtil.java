package com.capgemini.auth.security;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Cryptographic utility class responsible for signing, generating, and parsing JSON Web Tokens.
 * Utilizes the io.jsonwebtoken (JJWT) library with HMAC-SHA encryption.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Constructs the SecretKey object from the application properties secret string.
     * Required for both generating and verifying the signature of a token.
     *
     * @return SecretKey instance for HMAC-SHA.
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email) {
        return generateToken(email, null, null);
    }

    /**
     * Overloaded method to generate a JWT containing standard identity claims.
     *
     * @param email The user's email address (Subject).
     * @param userId The user's internal UUID or Long ID.
     * @param role The user's role (e.g., ADMIN, MENTOR) normalized to uppercase.
     * @return The serialized, signed JWT string.
     */
    public String generateToken(String email, Long userId, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role == null ? null : role.toUpperCase(Locale.ROOT))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getKey())
                .compact();
    }

    /**
     * Parses the JWT to extract the subject (email address).
     * Validates the token's signature cryptographically before parsing.
     *
     * @param token The raw JWT string.
     * @return The extracted email address.
     * @throws io.jsonwebtoken.JwtException If the token is invalid, expired, or malformed.
     */
    public String extractEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId instanceof String value && !value.isBlank()) {
            return Long.parseLong(value);
        }
        return null;
    }

    public String extractRole(String token) {
        Object role = extractAllClaims(token).get("role");
        return role == null ? null : role.toString();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * High-level validation method checking if the token matches the provided email.
     * Note: Token expiration is validated automatically inside the parser logic.
     *
     * @param token The raw JWT string.
     * @param email The expected email string.
     * @return true if the email matches the token subject.
     */
    public boolean isTokenValid(String token, String email) {
        return email.equals(extractEmail(token));
    }
}