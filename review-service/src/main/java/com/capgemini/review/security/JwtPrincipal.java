package com.capgemini.review.security;

public record JwtPrincipal(Long userId, String email, String role) {
}
