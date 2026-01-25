package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.response.dashboard.DashboardResponse;
import com.fullstack.bookShop.repository.*;
import com.fullstack.bookShop.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;

    private final BookRepository bookRepository;

    private final OrderRepository orderRepository;

    private final RatingRepository ratingRepository;

    private final CategoryRepository categoryRepository;

    @Override
    public DashboardResponse getDashBoardInformation() {
        Long countOrders = orderRepository.count();
        Long countBooks = bookRepository.count();
        Long countUsers = userRepository.count();
        Long countCategories = categoryRepository.count();
        Long countRatings = ratingRepository.count();

        return new DashboardResponse(
                countOrders,
                countUsers,
                countBooks,
                countCategories,
                countRatings
        );
    }
}
