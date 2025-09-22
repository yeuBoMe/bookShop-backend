package com.fullstack.bookShop.domain.dto.book;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestBookCreateDTO {
    private Long id;
    private String name;
    private String category;
    private Double price;
    private Integer quantity;
    private String image;
    private String author;
    private Instant createdAt;
    private String createdBy;
}
