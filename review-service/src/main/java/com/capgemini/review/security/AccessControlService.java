package com.capgemini.review.security;

import com.capgemini.review.dto.ReviewRequest;
import com.capgemini.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AccessControlService {

    private final ReviewRepository reviewRepository;

    public boolean canCreateReview(ReviewRequest request, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        return principal != null && Objects.equals(principal.userId(), request.getUserId());
    }

    public boolean canDeleteReview(Long reviewId, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        return principal != null && reviewRepository.findById(reviewId)
                .map(review -> Objects.equals(review.getUserId(), principal.userId()))
                .orElse(false);
    }

    private JwtPrincipal getPrincipal(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal
                ? principal
                : null;
    }
}
