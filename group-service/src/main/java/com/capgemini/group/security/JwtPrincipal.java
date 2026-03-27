package com.capgemini.group.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
