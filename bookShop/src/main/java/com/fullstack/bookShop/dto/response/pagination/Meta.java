package com.fullstack.bookShop.dto.response.pagination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Meta {
    private int current;
    private int pageSize;
    private int pages;
    private long total;
}
