package com.capgemini.mentor.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class MentorUpdateRequest {
    private String bio;

    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experience;

    @Min(value = 0, message = "Hourly rate cannot be negative")
    private Double hourlyRate;

    private Boolean available;
    private List<Long> skills;
}
