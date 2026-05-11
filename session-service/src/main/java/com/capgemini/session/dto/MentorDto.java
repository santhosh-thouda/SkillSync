package com.capgemini.session.dto;

import lombok.Data;

@Data
public class MentorDto {
    private Long id;
    private Long userId;
    private Double earnings;
    private Double hourlyRate;
}
