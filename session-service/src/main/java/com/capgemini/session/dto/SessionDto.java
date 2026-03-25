package com.capgemini.session.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    private Long id;
    private Long mentorId;
    private Long learnerId;
    private LocalDateTime sessionDate;
    private String status;
    private LocalDateTime createdAt;
}
