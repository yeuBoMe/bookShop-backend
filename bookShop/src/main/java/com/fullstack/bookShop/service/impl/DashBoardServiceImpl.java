package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.dashboard.RestDashboardDTO;
import com.fullstack.bookShop.repository.*;
import com.fullstack.bookShop.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashBoardServiceImpl implements DashboardService {

    private final BookRepository bookRepository;

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final RatingRepository ratingRepository;

    private final CategoryRepository categoryRepository;

    @Override
    public RestDashboardDTO getDashBoardInfo() {
        Long countOrders = orderRepository.count();
        Long countBooks = bookRepository.count();
        Long countUsers = userRepository.count();
        Long countCategories = categoryRepository.count();
        Long countRatings = ratingRepository.count();

        return new RestDashboardDTO(
                countOrders,
                countBooks,
                countUsers,
                countCategories,
                countRatings
        );
    }
}
