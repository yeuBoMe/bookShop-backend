package com.fullstack.bookShop.domain.dto.book;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

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
    private List<String> images;
    private String author;
    private Instant updatedAt;
    private String updatedBy;
}
