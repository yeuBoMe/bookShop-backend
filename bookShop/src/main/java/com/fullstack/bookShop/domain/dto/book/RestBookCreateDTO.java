package com.fullstack.bookShop.domain.dto.book;

import lombok.*;

import java.time.Instant;
import java.util.List;

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
    private List<String> images;
    private String author;
    private String introduceText;
    private String description;
    private String reader;
    private Instant createdAt;
    private String createdBy;
}
