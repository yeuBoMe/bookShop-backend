package com.fullstack.bookShop.domain.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestDashboardDTO {
    private Long countOrders;
    private Long countUsers;
    private Long countBooks;
    private Long countCategories;
    private Long countRatings;
}
