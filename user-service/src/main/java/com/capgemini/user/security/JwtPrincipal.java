package com.capgemini.user.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
