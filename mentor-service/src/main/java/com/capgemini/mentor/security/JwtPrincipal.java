package com.capgemini.mentor.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
