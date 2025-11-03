package com.fullstack.bookShop.domain.dto.rating;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestRatingCreateDTO {
    private Long id;
    private int stars;
    private String comment;
    private UserRatingCreateDTO user;
    private String book;
    private Instant createdAt;
    private String createdBy;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRatingCreateDTO {
        private Long id;
        private String name;
        private String avatar;
    }
}
