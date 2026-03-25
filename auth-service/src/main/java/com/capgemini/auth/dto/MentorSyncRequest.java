package com.capgemini.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class MentorSyncRequest {
    private Long userId;
    private String bio;
    private Integer experience;
    private Double hourlyRate;
    private List<Long> skills;
}
