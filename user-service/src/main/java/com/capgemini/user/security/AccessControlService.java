package com.capgemini.user.security;

import com.capgemini.user.dto.UserDto;
import com.capgemini.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AccessControlService {

    private final UserRepository userRepository;

    public boolean isCurrentUser(Long id, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        if (principal == null) {
            return false;
        }
        if (Objects.equals(principal.userId(), id)) {
            return true;
        }
        return userRepository.findById(id)
                .map(user -> user.getEmail().equalsIgnoreCase(principal.email()))
                .orElse(false);
    }

    public boolean hasEmail(Authentication authentication, String email) {
        JwtPrincipal principal = getPrincipal(authentication);
        return principal != null && principal.email().equalsIgnoreCase(email);
    }

    public boolean canCreateUser(UserDto request, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        if (principal == null) {
            return false;
        }
        return principal.email().equalsIgnoreCase(request.getEmail())
                && normalizeRole(principal.role()).equals(normalizeRole(request.getRole()));
    }

    private JwtPrincipal getPrincipal(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal
                ? principal
                : null;
    }

    private String normalizeRole(String role) {
        String normalizedRole = role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
        if (normalizedRole.startsWith("ROLE_")) {
            normalizedRole = normalizedRole.substring(5);
        }
        return "USER".equals(normalizedRole) ? "LEARNER" : normalizedRole;
    }
}
