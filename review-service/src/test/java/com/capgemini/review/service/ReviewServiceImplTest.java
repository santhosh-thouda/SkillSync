package com.capgemini.review.service;

import com.capgemini.review.dto.ReviewDto;
import com.capgemini.review.dto.ReviewRequest;
import com.capgemini.review.entity.Review;
import com.capgemini.review.mapper.ReviewMapper;
import com.capgemini.review.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Spy
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    void addReviewShouldPersistAndMapDto() {
        ReviewRequest request = new ReviewRequest();
        request.setMentorId(5L);
        request.setUserId(9L);
        request.setRating(4);
        request.setComment("Very helpful");

        Review savedReview = Review.builder()
                .id(1L)
                .mentorId(5L)
                .userId(9L)
                .rating(4)
                .comment("Very helpful")
                .build();

        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        ReviewDto response = reviewService.addReview(request);

        assertEquals(5L, response.getMentorId());
        assertEquals(9L, response.getUserId());
        assertEquals(4, response.getRating());
        assertEquals("Very helpful", response.getComment());
    }

    @Test
    void getReviewsByMentorShouldMapAllResults() {
        when(reviewRepository.findByMentorId(5L)).thenReturn(List.of(
                Review.builder().id(1L).mentorId(5L).userId(9L).rating(4).comment("Great").build(),
                Review.builder().id(2L).mentorId(5L).userId(10L).rating(5).comment("Excellent").build()
        ));

        List<ReviewDto> response = reviewService.getReviewsByMentor(5L);

        assertEquals(2, response.size());
        assertEquals("Great", response.get(0).getComment());
        assertEquals("Excellent", response.get(1).getComment());
    }
}
