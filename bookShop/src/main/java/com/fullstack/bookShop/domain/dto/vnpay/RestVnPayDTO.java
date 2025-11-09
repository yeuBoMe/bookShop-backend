package com.fullstack.bookShop.domain.dto.vnpay;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestVnPayDTO {
    private String paymentUrl;
}
