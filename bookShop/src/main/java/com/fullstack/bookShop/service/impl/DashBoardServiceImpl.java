package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.domain.dto.dashboard.RestDashboardDTO;
import com.fullstack.bookShop.repository.BookRepository;
import com.fullstack.bookShop.repository.OrderRepository;
import com.fullstack.bookShop.repository.UserRepository;
import com.fullstack.bookShop.service.DashboardService;
import org.springframework.stereotype.Service;

@Service
public class DashBoardServiceImpl implements DashboardService {

    private final BookRepository bookRepository;

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    public DashBoardServiceImpl(
            BookRepository bookRepository,
            OrderRepository orderRepository,
            UserRepository userRepository
    ) {
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RestDashboardDTO getDashBoardInfo() {
        Long countOrders = this.orderRepository.count();
        Long countBooks = this.bookRepository.count();
        Long countUsers = this.userRepository.count();

        return new RestDashboardDTO(countOrders, countBooks, countUsers);
    }
}
