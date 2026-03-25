package com.capgemini.review.service;

import com.capgemini.review.dto.ReviewDto;
import com.capgemini.review.dto.ReviewRequest;

import java.util.List;

public interface ReviewService {
    ReviewDto addReview(ReviewRequest request);
    List<ReviewDto> getReviewsByMentor(Long mentorId);
    void deleteReview(Long id);
}
