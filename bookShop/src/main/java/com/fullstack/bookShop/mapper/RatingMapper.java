package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.RatingResponse;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Rating;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RatingMapper {

    public RatingResponse toResponse(Rating rating, String methodName) {
        RatingResponse.RatingResponseBuilder builder = RatingResponse.builder()
                .id(rating.getId())
                .stars(rating.getStars())
                .comment(rating.getComment())
                .user(convertUser(rating))
                .book(convertBookName(rating));

        if (ActionConstant.CREATE.equals(methodName)) {
            builder.createdAt(rating.getCreatedAt())
                    .createdBy(rating.getCreatedBy());
        } else if (ActionConstant.UPDATE.equals(methodName)) {
            builder.updatedAt(rating.getUpdatedAt())
                    .updatedBy(rating.getUpdatedBy());
        } else if (ActionConstant.FETCH.equals(methodName)) {
            builder.createdAt(rating.getCreatedAt())
                    .updatedAt(rating.getUpdatedAt())
                    .createdBy(rating.getCreatedBy())
                    .updatedBy(rating.getUpdatedBy());
        }

        return builder.build();
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
