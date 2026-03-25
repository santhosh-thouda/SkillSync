package com.capgemini.review.mapper;

import com.capgemini.review.dto.ReviewDto;
import com.capgemini.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewDto toDto(Review review) {
        return new ReviewDto(
                review.getId(),
                review.getMentorId(),
                review.getUserId(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
