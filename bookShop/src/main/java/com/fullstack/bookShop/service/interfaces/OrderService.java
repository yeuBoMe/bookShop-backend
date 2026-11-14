package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.request.OrderStatusRequest;
import com.fullstack.bookShop.dto.response.OrderResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.model.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface OrderService {

    PaginationResponse fetchAllOrders(Specification<Order> spec, Pageable pageable);

    PaginationResponse fetchListOrdersHistory(String email, Pageable pageable);

    Order createOrder(Order orderRequest);

    Order partialUpdateOrderById(Long id, OrderStatusRequest orderStatusRequest);

    Order getOrderById(Long id);

    OrderResponse getOrderResponseById(Long id);

    OrderResponse partialUpdateOrderResponseById(Long id, OrderStatusRequest orderStatusRequest);

    boolean checkIdExists(Long id);

    void deleteOrderById(Long id);
}
