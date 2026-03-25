package com.capgemini.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long mentorId;
    private Long userId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
