package com.capgemini.session.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
