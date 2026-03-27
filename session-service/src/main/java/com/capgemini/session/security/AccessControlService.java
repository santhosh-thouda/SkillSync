package com.capgemini.session.security;

import com.capgemini.session.dto.SessionRequest;
import com.capgemini.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AccessControlService {

    private final SessionRepository sessionRepository;

    public boolean canRequestSession(SessionRequest request, Authentication authentication) {
        return isCurrentUser(request.getLearnerId(), authentication);
    }

    public boolean isCurrentUser(Long userId, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        return principal != null && Objects.equals(principal.userId(), userId);
    }

    public boolean isLearnerSessionOwner(Long sessionId, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        return principal != null && sessionRepository.findById(sessionId)
                .map(session -> Objects.equals(session.getLearnerId(), principal.userId()))
                .orElse(false);
    }

    public boolean isMentorSessionOwner(Long sessionId, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        return principal != null && sessionRepository.findById(sessionId)
                .map(session -> Objects.equals(session.getMentorId(), principal.userId()))
                .orElse(false);
    }

    public boolean canDeleteSession(Long sessionId, Authentication authentication) {
        return isLearnerSessionOwner(sessionId, authentication) || isMentorSessionOwner(sessionId, authentication);
    }

    private JwtPrincipal getPrincipal(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal
                ? principal
                : null;
    }
}
