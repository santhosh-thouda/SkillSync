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

/**
 * REST Controller for the Mentor domain.
 * Manages mentor profile applications, approvals, availability, and earnings.
 * Exposes methods to both regular users (for catalog viewing) and ADMINs/mentors (for profile management).
 */
@RestController
@RequestMapping("/mentors")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;

    /**
     * Allows an authenticated user to submit an application to become a mentor.
     *
     * @param request Contains the user's bio, skills, and hourly rate.
     * @return The newly created pending mentor profile.
     */
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

    /**
     * Approves a pending mentor application.
     * Typically executed by an ADMIN user via a back-office dashboard.
     *
     * @param id The mentor profile ID to approve.
     * @return The newly approved MentorDto.
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MentorDto> approveMentor(@PathVariable Long id) {
        return ResponseEntity.ok(mentorService.approveMentor(id));
    }

    /**
     * Increments the total earnings of a mentor.
     * Called synchronously by the Session Service when a session is completed.
     *
     * @param id The mentor profile ID.
     * @param amount The monetary amount to add.
     * @return The updated MentorDto.
     */
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
 