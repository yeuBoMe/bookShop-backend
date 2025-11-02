package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.domain.dto.dashboard.RestDashboardDTO;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.CategoryRepository;
import com.fullstack.bookShop.repository.OrderRepository;
import com.fullstack.bookShop.repository.UserRepository;
import com.fullstack.bookShop.service.DashboardService;
import org.springframework.stereotype.Service;

@Service
public class DashBoardServiceImpl implements DashboardService {

    private final BookRepository bookRepository;

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    public DashBoardServiceImpl(
            BookRepository bookRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository
    ) {
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public RestDashboardDTO getDashBoardInfo() {
        Long countOrders = this.orderRepository.count();
        Long countBooks = this.bookRepository.count();
        Long countUsers = this.userRepository.count();
        Long countCategories = this.categoryRepository.count();

        return new RestDashboardDTO(countOrders, countBooks, countUsers, countCategories);
    }
}
