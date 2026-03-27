package com.capgemini.mentor.security;

import com.capgemini.mentor.dto.MentorApplyRequest;
import com.capgemini.mentor.repository.MentorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AccessControlService {

    private final MentorRepository mentorRepository;

    public boolean canApplyForMentor(MentorApplyRequest request, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        return principal != null && Objects.equals(principal.userId(), request.getUserId());
    }

    public boolean isMentorOwner(Long mentorId, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        return principal != null && mentorRepository.findById(mentorId)
                .map(mentor -> Objects.equals(mentor.getUserId(), principal.userId()))
                .orElse(false);
    }

    private JwtPrincipal getPrincipal(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal
                ? principal
                : null;
    }
}
