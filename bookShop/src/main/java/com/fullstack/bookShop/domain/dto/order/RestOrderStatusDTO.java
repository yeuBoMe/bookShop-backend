package com.fullstack.bookShop.domain.dto.order;

import com.fullstack.bookShop.util.constant.StatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RestOrderStatusDTO {

    @NotNull(message = "Status is required")
    private StatusEnum status;
}
