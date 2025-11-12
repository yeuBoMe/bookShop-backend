package com.fullstack.bookShop.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestOrderDetailViewDTO {
    private Long id;
    private int quantity;
    private String bookName;
    private Double price;
}
