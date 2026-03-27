package com.capgemini.skill.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
