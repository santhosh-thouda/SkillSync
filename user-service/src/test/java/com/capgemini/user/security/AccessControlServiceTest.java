package com.capgemini.user.security;

import com.capgemini.user.dto.UserDto;
import com.capgemini.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AccessControlServiceTest {

    private final AccessControlService accessControlService = new AccessControlService(mock(UserRepository.class));

    @Test
    void canCreateUserAllowsRolePrefixDifferenceForLearnerSync() {
        Authentication authentication = authenticatedPrincipal("alex@example.com", "ROLE_LEARNER");
        UserDto request = new UserDto(null, "Alex", "alex@example.com", "LEARNER", null, null);

        assertTrue(accessControlService.canCreateUser(request, authentication));
    }

    @Test
    void canCreateUserTreatsUserRoleAsLearnerAlias() {
        Authentication authentication = authenticatedPrincipal("alex@example.com", "ROLE_USER");
        UserDto request = new UserDto(null, "Alex", "alex@example.com", "LEARNER", null, null);

        assertTrue(accessControlService.canCreateUser(request, authentication));
    }

    @Test
    void canCreateUserRejectsDifferentEmail() {
        Authentication authentication = authenticatedPrincipal("alex@example.com", "ROLE_LEARNER");
        UserDto request = new UserDto(null, "Taylor", "taylor@example.com", "LEARNER", null, null);

        assertFalse(accessControlService.canCreateUser(request, authentication));
    }

    private Authentication authenticatedPrincipal(String email, String role) {
        JwtPrincipal principal = new JwtPrincipal(1L, email, role);
        return new UsernamePasswordAuthenticationToken(principal, null);
    }
}
