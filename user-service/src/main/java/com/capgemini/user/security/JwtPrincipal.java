package com.capgemini.user.security;

// JwtPrincipal class for holding JWT claims
public record JwtPrincipal(Long userId, String email, String role) {
}
