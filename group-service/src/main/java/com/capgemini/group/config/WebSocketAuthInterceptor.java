package com.capgemini.group.config;

import com.capgemini.group.security.JwtPrincipal;
import com.capgemini.group.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Intercepts STOMP CONNECT frames and authenticates the user from the
 * JWT token carried in the STOMP "Authorization" header.
 *
 * This is necessary because Spring Security's HTTP filter chain only
 * processes the HTTP upgrade handshake — it does NOT see STOMP-level
 * headers on subsequent frames. Without this interceptor the STOMP
 * session has no authenticated principal, causing @MessageMapping
 * methods to run as anonymous and member-checks to fail silently.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenService jwtTokenService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        // Only authenticate on CONNECT frames
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    JwtPrincipal principal = jwtTokenService.extractPrincipal(token);

                    String role = principal.role() == null
                            ? "USER"
                            : principal.role().toUpperCase(Locale.ROOT);
                    if (role.startsWith("ROLE_")) role = role.substring(5);

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                            );

                    // Attach the authenticated user to the STOMP session
                    accessor.setUser(auth);
                    log.debug("WebSocket authenticated: userId={}, role={}",
                            principal.userId(), principal.role());

                } catch (Exception e) {
                    log.warn("WebSocket JWT authentication failed: {}", e.getMessage());
                    // Don't throw — let the connection proceed as anonymous;
                    // the @MessageMapping member-check will reject unauthorised sends.
                }
            } else {
                log.debug("WebSocket CONNECT received without Authorization header");
            }
        }

        return message;
    }
}
