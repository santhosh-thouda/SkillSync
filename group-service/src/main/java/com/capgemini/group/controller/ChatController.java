package com.capgemini.group.controller;

import com.capgemini.group.dto.ChatMessageDto;
import com.capgemini.group.dto.SendMessageRequest;
import com.capgemini.group.entity.ChatMessage;
import com.capgemini.group.repository.ChatMessageRepository;
import com.capgemini.group.security.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * WebSocket endpoint — client sends to /app/sendMessage
     */
    @MessageMapping("/sendMessage")
    public void sendMessage(@Payload SendMessageRequest request, Principal stompPrincipal) {

        Long authenticatedUserId = resolveUserId(stompPrincipal, request.getSenderId());
        String authenticatedName  = resolveSenderName(stompPrincipal, request.getSenderName());

        if (authenticatedUserId == null) {
            log.warn("Unauthenticated WebSocket send attempt");
            return;
        }

        // 1. Save to PostgreSQL using the new table
        ChatMessage saved = chatMessageRepository.save(
                ChatMessage.builder()
                        .groupId(request.getGroupId())
                        .senderId(authenticatedUserId)
                        .senderName(authenticatedName)
                        .content(request.getContent())
                        .messageType("TEXT")
                        .build()
        );

        // 2. Broadcast to all subscribers of this group
        ChatMessageDto dto = ChatMessageDto.builder()
                .id(saved.getId())
                .groupId(saved.getGroupId())
                .senderId(saved.getSenderId())
                .senderName(saved.getSenderName())
                .content(saved.getContent())
                .sentAt(saved.getCreatedAt())
                .build();

        String destination = "/topic/group/" + request.getGroupId();
        messagingTemplate.convertAndSend(destination, dto);
        
        log.info("Message saved (ID: {}) and broadcasted to group {}", saved.getId(), request.getGroupId());
    }

    /**
     * REST API — load message history
     */
    @GetMapping("/groups/{groupId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ChatMessageDto>> getMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "100") int limit) {

        int safeLimit = Math.min(Math.max(limit, 1), 500);

        List<ChatMessageDto> messages = chatMessageRepository
                .findByGroupId(groupId, PageRequest.of(0, safeLimit, Sort.by("createdAt").descending()))
                .stream()
                .map(m -> ChatMessageDto.builder()
                        .id(m.getId())
                        .groupId(m.getGroupId())
                        .senderId(m.getSenderId())
                        .senderName(m.getSenderName())
                        .content(m.getContent())
                        .sentAt(m.getCreatedAt())
                        .build())
                .sorted((a, b) -> {
                    if (a.getSentAt() == null) return -1;
                    if (b.getSentAt() == null) return 1;
                    return a.getSentAt().compareTo(b.getSentAt());
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(messages);
    }

    /**
     * REST API — mark messages in a group as read for the current user
     */
    @PostMapping("/groups/{groupId}/messages/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markMessagesAsRead(
            @PathVariable Long groupId,
            @RequestParam Long userId) {
        
        log.info("Marking messages as read for group {} and user {}", groupId, userId);
        // In a real system, we'd find all unread messages and create ChatMessageStatus entries.
        // For now, this is a placeholder to show how it would be structured.
        
        return ResponseEntity.ok().build();
    }

    private Long resolveUserId(Principal stompPrincipal, Long fallback) {
        if (stompPrincipal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof JwtPrincipal jwt) {
            return jwt.userId();
        }
        return fallback;
    }

    private String resolveSenderName(Principal stompPrincipal, String fallback) {
        if (stompPrincipal instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof JwtPrincipal jwt) {
            if (jwt.name() != null && !jwt.name().isBlank()) return jwt.name();
            return jwt.email() != null ? jwt.email().split("@")[0] : fallback;
        }
        return fallback != null ? fallback : "Guest";
    }
}
