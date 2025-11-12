package com.fullstack.bookShop.service;

import com.fullstack.bookShop.dto.order.RestOrderCreateDTO;
import com.fullstack.bookShop.dto.order.RestOrderUpdateDTO;
import com.fullstack.bookShop.dto.order.RestOrderViewDTO;
import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface OrderService {
    Order createOrder(Order order);
    Order updateOrderPartialById(Long id, Order orderRequest);
    Order getOrderById(Long id);

    boolean checkIdExists(Long id);
    void deleteOrderById(Long id);

    RestOrderCreateDTO convertOrderToRestOrderCreateDTO(Order order);
    RestOrderUpdateDTO convertOrderToRestOrderUpdateDTO(Order order);
    RestOrderViewDTO convertOrderToRestOrderViewDTO(Order order);

    ResultPaginationDTO fetchListOrders(Specification<Order> spec, Pageable pageable);
    ResultPaginationDTO fetchListOrdersHistory(String email, Pageable pageable);
}
