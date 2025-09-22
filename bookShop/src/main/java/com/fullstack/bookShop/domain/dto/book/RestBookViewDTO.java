package com.fullstack.bookShop.domain.dto.book;

import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestBookViewDTO {
    private Long id;
    private String name;
    private String category;
    private Double price;
    private Integer quantity;
    private String image;
    private String author;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
