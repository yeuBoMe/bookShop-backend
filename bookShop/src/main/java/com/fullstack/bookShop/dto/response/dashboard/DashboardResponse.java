package com.fullstack.bookShop.dto.response.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Long countOrders;
    private Long countUsers;
    private Long countBooks;
    private Long countCategories;
    private Long countRatings;
}
