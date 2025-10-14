package com.fullstack.bookShop.domain.dto.order;

import com.fullstack.bookShop.domain.entity.OrderDetail;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class RestOrderCreateDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String paymentMethod;
    private Double totalPrice;
    private List<RestOrderDetailViewDTO> detail;
    private Instant createdAt;
    private String createdBy;
}
