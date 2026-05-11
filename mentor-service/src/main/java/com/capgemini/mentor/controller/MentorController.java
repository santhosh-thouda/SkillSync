package com.capgemini.mentor.controller;

import com.capgemini.mentor.dto.AvailabilityUpdateRequest;
import com.capgemini.mentor.dto.MentorApplyRequest;
import com.capgemini.mentor.dto.MentorDto;
import com.capgemini.mentor.dto.MentorUpdateRequest;
import com.capgemini.mentor.service.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    @PostMapping("/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MentorDto> applyForMentor(@Valid @RequestBody MentorApplyRequest request) {
        return new ResponseEntity<>(mentorService.applyForMentor(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MentorDto>> getAllMentors() {
        return ResponseEntity.ok(mentorService.getAllMentors());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MentorDto> getMentorById(@PathVariable Long id) {
        return ResponseEntity.ok(mentorService.getMentorById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MentorDto> getMentorByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(mentorService.getMentorByUserId(userId));
    }

    @PutMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN') or @accessControlService.isMentorOwner(#id, authentication)")
    public ResponseEntity<MentorDto> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityUpdateRequest request) {
        return ResponseEntity.ok(mentorService.updateAvailability(id, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accessControlService.isMentorOwner(#id, authentication)")
    public ResponseEntity<MentorDto> updateMentor(
            @PathVariable Long id,
            @Valid @RequestBody MentorUpdateRequest request) {
        return ResponseEntity.ok(mentorService.updateMentor(id, request));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MentorDto> approveMentor(@PathVariable Long id) {
        return ResponseEntity.ok(mentorService.approveMentor(id));
    }

    @PutMapping("/{id}/earnings")
    public ResponseEntity<MentorDto> addEarnings(@PathVariable Long id, @RequestParam Double amount) {
        return ResponseEntity.ok(mentorService.addEarnings(id, amount));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accessControlService.isMentorOwner(#id, authentication)")
    public ResponseEntity<Void> deleteMentor(@PathVariable Long id) {
        mentorService.deleteMentor(id);
        return ResponseEntity.noContent().build();
    }
}
 