package com.fullstack.bookShop.domain.dto.order;

import com.fullstack.bookShop.domain.entity.OrderDetail;
import com.fullstack.bookShop.util.constant.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestOrderViewDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String paymentMethod;
    private Double totalPrice;
    private StatusEnum status;
    private List<RestOrderDetailViewDTO> detail;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;
}
