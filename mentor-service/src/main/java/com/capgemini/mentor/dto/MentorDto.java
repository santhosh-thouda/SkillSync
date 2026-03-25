package com.capgemini.mentor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MentorDto {
    private Long id;
    private Long userId;
    private String bio;
    private Integer experience;
    private Double rating;
    private Double hourlyRate;
    private boolean available;
    private Boolean approved;
    private List<Long> skills;
    private LocalDateTime createdAt;
}
