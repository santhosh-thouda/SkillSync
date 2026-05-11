package com.capgemini.session.security;

import com.capgemini.session.client.MentorServiceClient;
import com.capgemini.session.dto.MentorDto;
import com.capgemini.session.dto.SessionRequest;
import jakarta.servlet.http.HttpServletRequest;
import com.capgemini.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AccessControlService {

    private final SessionRepository sessionRepository;
    private final MentorServiceClient mentorServiceClient;
    private final HttpServletRequest request;

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
                .map(session -> Objects.equals(session.getMentorId(), principal.userId())
                        || isMentorProfileOwner(session.getMentorId(), authentication))
                .orElse(false);
    }

    public boolean isMentorProfileOwner(Long mentorId, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        if (principal == null) {
            return false;
        }
        if (Objects.equals(mentorId, principal.userId())) {
            return true;
        }
        try {
            MentorDto mentor = mentorServiceClient.getMentorById(request.getHeader("Authorization"), mentorId);
            return mentor != null && Objects.equals(mentor.getUserId(), principal.userId());
        } catch (Exception ignored) {
            return false;
        }
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
