package com.fullstack.bookShop.dto.response;

import com.fullstack.bookShop.util.enums.StatusEnum;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String paymentMethod;
    private Double totalPrice;
    private StatusEnum status;
    private List<OrderDetailResponse> detail;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
