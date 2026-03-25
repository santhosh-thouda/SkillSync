package com.capgemini.review.service;

import com.capgemini.review.dto.ReviewDto;
import com.capgemini.review.dto.ReviewRequest;
import com.capgemini.review.entity.Review;
import com.capgemini.review.exception.ResourceNotFoundException;
import com.capgemini.review.mapper.ReviewMapper;
import com.capgemini.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public ReviewDto addReview(ReviewRequest request) {
        Review review = Review.builder()
                .mentorId(request.getMentorId())
                .userId(request.getUserId())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
                
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toDto(savedReview);
    }

    @Override
    public List<ReviewDto> getReviewsByMentor(Long mentorId) {
        return reviewRepository.findByMentorId(mentorId).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        reviewRepository.delete(review);
    }
}
