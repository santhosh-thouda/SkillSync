package com.capgemini.session.controller;

import com.capgemini.session.dto.SessionDto;
import com.capgemini.session.dto.SessionRequest;
import com.capgemini.session.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<SessionDto> requestSession(@Valid @RequestBody SessionRequest request) {
        return new ResponseEntity<>(sessionService.requestSession(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<SessionDto> acceptSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.updateSessionStatus(id, "ACCEPTED"));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<SessionDto> rejectSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.updateSessionStatus(id, "REJECTED"));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<SessionDto> cancelSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.updateSessionStatus(id, "CANCELLED"));
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<SessionDto> completeSession(@PathVariable Long id) {
        return ResponseEntity.ok(sessionService.updateSessionStatus(id, "COMPLETED"));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SessionDto>> getSessionsByUser(@PathVariable Long userId) {
        // Here we just use getSessionsByLearner as an example for the user view
        return ResponseEntity.ok(sessionService.getSessionsByLearner(userId));
    }
    
    @GetMapping("/mentor/{mentorId}")
    public ResponseEntity<List<SessionDto>> getSessionsByMentor(@PathVariable Long mentorId) {
        return ResponseEntity.ok(sessionService.getSessionsByMentor(mentorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }
}
