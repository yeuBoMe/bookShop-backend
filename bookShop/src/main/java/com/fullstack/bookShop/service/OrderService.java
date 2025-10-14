package com.fullstack.bookShop.service;

import com.fullstack.bookShop.domain.dto.order.RestOrderCreateDTO;
import com.fullstack.bookShop.domain.dto.order.RestOrderUpdateDTO;
import com.fullstack.bookShop.domain.dto.order.RestOrderViewDTO;
import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface OrderService {
    Order createOrder(Order order);
    Order updateOrderById(Long id, Order order);
    Order getOrderById(Long id);

    boolean checkIdExists(Long id);
    void deleteOrderById(Long id);

    RestOrderCreateDTO convertOrderToRestOrderCreateDTO(Order order);
    RestOrderUpdateDTO convertOrderToRestOrderUpdateDTO(Order order);
    RestOrderViewDTO convertOrderToRestOrderViewDTO(Order order);

    ResultPaginationDTO fetchListOrders(Specification<Order> spec, Pageable pageable);
}
