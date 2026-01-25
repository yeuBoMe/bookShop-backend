package com.fullstack.bookShop.dto.request;

import com.fullstack.bookShop.util.constant.entity.OrderConstant;
import com.fullstack.bookShop.util.enums.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusRequest {

    @NotNull(message = OrderConstant.STATUS_REQUIRED)
    private StatusEnum status;
}
