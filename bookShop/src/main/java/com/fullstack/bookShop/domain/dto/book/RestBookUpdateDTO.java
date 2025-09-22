package com.fullstack.bookShop.domain.dto.book;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestBookUpdateDTO {
    private Long id;
    private String name;
    private String category;
    private Double price;
    private Integer quantity;
    private String image;
    private String author;
    private Instant updatedAt;
    private String updatedBy;
}
