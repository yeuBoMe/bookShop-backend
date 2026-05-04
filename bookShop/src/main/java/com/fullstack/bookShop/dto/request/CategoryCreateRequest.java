package com.fullstack.bookShop.dto.request;

import com.fullstack.bookShop.util.constant.entity.CategoryConstant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {

    @NotBlank(message = CategoryConstant.NAME_REQUIRED)
    @Size(max = 100, message = CategoryConstant.NAME_MAX_LENGTH)
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ0-9'\\-\\.\\:\\s]{1,100}$",
            message = CategoryConstant.NAME_INVALID
    )
    private String name;
}
