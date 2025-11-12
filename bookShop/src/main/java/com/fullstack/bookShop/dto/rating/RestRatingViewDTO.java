package com.fullstack.bookShop.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestRatingViewDTO {
    private Long id;
    private int stars;
    private String comment;
    private UserRatingViewDTO user;
    private String book;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRatingViewDTO {
        private Long id;
        private String name;
        private String avatar;
    }
}
