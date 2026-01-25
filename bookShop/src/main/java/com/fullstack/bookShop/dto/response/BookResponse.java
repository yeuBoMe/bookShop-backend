package com.fullstack.bookShop.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String name;
    private String category;
    private Double price;
    private Integer quantity;
    private List<String> images;
    private String author;
    private String introduceText;
    private String description;
    private String reader;
    private Double averageStars;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
