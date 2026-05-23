package com.capgemini.session.controller;

import com.capgemini.session.dto.SessionDto;
import com.capgemini.session.dto.SessionRequest;
import com.capgemini.session.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for the Mentorship Session domain.
 * Governs the state machine of a mentorship booking (PENDING -> ACCEPTED -> COMPLETED).
 * Provides secure endpoints to book, manage, and retrieve historical sessions.
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /**
     Starts the state machine with a status of PENDING.
    **/
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('LEARNER') and @accessControlService.canRequestSession(#request, authentication))")
    public ResponseEntity<SessionDto> requestSession(@Valid @RequestBody SessionRequest request) {
        return new ResponseEntity<>(sessionService.requestSession(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SessionDto> acceptSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.updateSessionStatus(id, "ACCEPTED"));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SessionDto> rejectSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.updateSessionStatus(id, "REJECTED"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @accessControlService.isLearnerSessionOwner(#id, authentication)")
    public ResponseEntity<SessionDto> cancelSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.updateSessionStatus(id, "CANCELLED"));
    }
   
    @PutMapping("/{id}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SessionDto> completeSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.updateSessionStatus(id, "COMPLETED"));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @accessControlService.isCurrentUser(#userId, authentication)")
    public ResponseEntity<List<SessionDto>> getSessionsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(sessionService.getSessionsByLearner(userId));
    }
    
    @GetMapping("/mentor/{mentorId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SessionDto>> getSessionsByMentor(@PathVariable Long mentorId) {
        return ResponseEntity.ok(sessionService.getSessionsByMentor(mentorId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accessControlService.canDeleteSession(#id, authentication)")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
