package com.fullstack.bookShop.dto.response;

import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {
    private Long id;
    private int stars;
    private String comment;
    private UserRatingResponse user;
    private String book;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRatingResponse {
        private Long id;
        private String name;
        private String avatar;
    }
}
