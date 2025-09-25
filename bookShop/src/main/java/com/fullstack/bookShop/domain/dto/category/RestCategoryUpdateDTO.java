package com.fullstack.bookShop.domain.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestCategoryUpdateDTO {
    private Long id;
    private String name;
    private Instant updatedAt;
    private String updatedBy;
}
