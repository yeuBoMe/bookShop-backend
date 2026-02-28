package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.OrderDetailResponse;
import com.fullstack.bookShop.dto.response.OrderResponse;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Order;
import com.fullstack.bookShop.model.OrderDetail;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order, String methodName) {
        OrderResponse.OrderResponseBuilder builder = OrderResponse.builder()
                .id(order.getId())
                .name(order.getName())
                .email(order.getEmail())
                .phone(order.getPhone())
                .address(order.getAddress())
                .paymentMethod(order.getPaymentMethod())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .detail(convertListOrderDetails(order.getDetail()));

        if (ActionConstant.CREATE.equals(methodName)) {
            builder.createdAt(order.getCreatedAt())
                    .createdBy(order.getCreatedBy());
        } else if (ActionConstant.UPDATE.equals(methodName)) {
            builder.updatedAt(order.getUpdatedAt())
                    .updatedBy(order.getUpdatedBy());
        } else if (ActionConstant.FETCH.equals(methodName)) {
            builder.createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .createdBy(order.getCreatedBy())
                    .updatedBy(order.getUpdatedBy());
        }

        return builder.build();
    }

    private List<OrderDetailResponse> convertListOrderDetails(List<OrderDetail> orderDetailList) {
        return Optional.ofNullable(orderDetailList)
                .orElse(List.of())
                .stream()
                .map(orderDetail -> new OrderDetailResponse(
                        orderDetail.getId(),
                        orderDetail.getQuantity(),
                        convertBookName(orderDetail),
                        convertBookPrice(orderDetail)
                ))
                .toList();
    }

    private String convertBookName(OrderDetail orderDetail) {
        return Optional.ofNullable(orderDetail.getBook())
                .map(Book::getName)
                .orElse(null);
    }

    private Double convertBookPrice(OrderDetail orderDetail) {
        return Optional.ofNullable(orderDetail.getBook())
                .map(Book::getPrice)
                .orElse(null);
    }
}
