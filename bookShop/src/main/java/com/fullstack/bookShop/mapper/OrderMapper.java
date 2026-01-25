package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.OrderDetailResponse;
import com.fullstack.bookShop.dto.response.OrderResponse;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Order;
import com.fullstack.bookShop.model.OrderDetail;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderMapper {

    public OrderResponse convertOrderToCreateResponse(Order order){
        return OrderResponse.builder()
                .id(order.getId())
                .name(order.getName())
                .email(order.getEmail())
                .phone(order.getPhone())
                .address(order.getAddress())
                .paymentMethod(order.getPaymentMethod())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .detail(convertListOrderDetails(order.getDetail()))
                .createdAt(order.getCreatedAt())
                .createdBy(order.getCreatedBy())
                .build();
    }

    public OrderResponse convertOrderToUpdateResponse(Order order){
        return OrderResponse.builder()
                .id(order.getId())
                .name(order.getName())
                .email(order.getEmail())
                .phone(order.getPhone())
                .address(order.getAddress())
                .paymentMethod(order.getPaymentMethod())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .detail(convertListOrderDetails(order.getDetail()))
                .updatedAt(order.getUpdatedAt())
                .updatedBy(order.getUpdatedBy())
                .build();
    }

    public OrderResponse convertOrderToViewResponse(Order order){
        return OrderResponse.builder()
                .id(order.getId())
                .name(order.getName())
                .email(order.getEmail())
                .phone(order.getPhone())
                .address(order.getAddress())
                .paymentMethod(order.getPaymentMethod())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .detail(convertListOrderDetails(order.getDetail()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .createdBy(order.getCreatedBy())
                .updatedBy(order.getUpdatedBy())
                .build();
    }

    private List<OrderDetailResponse> convertListOrderDetails(List<OrderDetail> orderDetailList) {
        return Optional.ofNullable(orderDetailList)
                .orElse(List.of())
                .stream()
                .map(orderDetail -> new OrderDetailResponse(
                        orderDetail.getId(),
                        orderDetail.getQuantity(),
                        convertBookName(orderDetail),
                        convertPrice(orderDetail)
                ))
                .toList();
    }

    private String convertBookName(OrderDetail orderDetail) {
        return Optional.ofNullable(orderDetail.getBook())
                .map(Book::getName)
                .orElse(null);
    }

    private Double convertPrice(OrderDetail orderDetail) {
        return Optional.ofNullable(orderDetail.getBook())
                .map(Book::getPrice)
                .orElse(null);
    }
}
