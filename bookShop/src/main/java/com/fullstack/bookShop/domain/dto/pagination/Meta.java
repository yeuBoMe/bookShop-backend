package com.fullstack.bookShop.domain.dto.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Meta {
    private int current;
    private int pageSize;
    private int pages;
    private long total;
}
