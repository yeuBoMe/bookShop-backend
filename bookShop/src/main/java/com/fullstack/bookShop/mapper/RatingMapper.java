package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.RatingResponse;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Rating;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RatingMapper {

    public RatingResponse convertRatingToCreateResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .stars(rating.getStars())
                .comment(rating.getComment())
                .user(convertUser(rating))
                .book(convertBookName(rating))
                .createdAt(rating.getCreatedAt())
                .createdBy(rating.getCreatedBy())
                .build();
    }

    public RatingResponse convertRatingToViewResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .stars(rating.getStars())
                .comment(rating.getComment())
                .user(convertUser(rating))
                .book(convertBookName(rating))
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .createdBy(rating.getCreatedBy())
                .updatedBy(rating.getUpdatedBy())
                .build();
    }

    private RatingResponse.UserRatingResponse convertUser(Rating rating) {
        return Optional.ofNullable(rating.getUser())
                .map(user -> new RatingResponse.UserRatingResponse(
                        user.getId(),
                        user.getFullName(),
                        user.getAvatar()
                ))
                .orElse(null);
    }

    private String convertBookName(Rating rating) {
        return Optional.ofNullable(rating.getBook())
                .map(Book::getName)
                .orElse(null);
    }
}
