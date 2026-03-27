package com.capgemini.group.security;

import com.capgemini.group.dto.GroupRequest;
import com.capgemini.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AccessControlService {

    private final GroupRepository groupRepository;

    public boolean canCreateGroup(GroupRequest request, Authentication authentication) {
        return isCurrentUser(request.getCreatedBy(), authentication);
    }

    public boolean isCurrentUser(Long userId, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        return principal != null && Objects.equals(principal.userId(), userId);
    }

    public boolean isGroupOwner(Long groupId, Authentication authentication) {
        JwtPrincipal principal = getPrincipal(authentication);
        return principal != null && groupRepository.findById(groupId)
                .map(group -> Objects.equals(group.getCreatedBy(), principal.userId()))
                .orElse(false);
    }

    private JwtPrincipal getPrincipal(Authentication authentication) {
        return authentication != null && authentication.getPrincipal() instanceof JwtPrincipal principal
                ? principal
                : null;
    }
}
