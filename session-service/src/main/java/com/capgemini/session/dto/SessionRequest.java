package com.capgemini.session.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionRequest {
    
    @NotNull(message = "Mentor ID is required")
    private Long mentorId;

    @NotNull(message = "Learner ID is required")
    private Long learnerId;

    @NotNull(message = "Session date is required")
    @Future(message = "Session date must be in the future")
    private LocalDateTime sessionDate;
}
