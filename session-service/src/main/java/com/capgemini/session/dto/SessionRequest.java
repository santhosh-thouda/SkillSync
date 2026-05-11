package com.capgemini.session.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private String mentorName;
    private String learnerName;
    private Double hourlyRate;

    @NotNull(message = "Session date is required")
    @Future(message = "Session date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime sessionDate;
}
