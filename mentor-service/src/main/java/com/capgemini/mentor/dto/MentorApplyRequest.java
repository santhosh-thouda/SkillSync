package com.capgemini.mentor.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MentorApplyRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String bio;
    
    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experience;
    
    @Min(value = 0, message = "Hourly rate cannot be negative")
    private Double hourlyRate;
    
    private List<Long> skills;
}
