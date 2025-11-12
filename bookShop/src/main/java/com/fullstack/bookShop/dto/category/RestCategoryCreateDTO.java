package com.fullstack.bookShop.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestCategoryCreateDTO {
    private Long id;
    private String name;
    private Instant createdAt;
    private String createdBy;
}
