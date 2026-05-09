package com.fullstack.bookShop.dto.request;

import com.fullstack.bookShop.util.constant.entity.BookConstant;
import com.fullstack.bookShop.util.constant.entity.CategoryConstant;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateRequest {

    @NotBlank(message = BookConstant.NAME_REQUIRED)
    @Size(max = 100, message = BookConstant.NAME_MAX_LENGTH)
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ0-9'\\-\\.\\:\\s]{1,100}$",
            message = BookConstant.NAME_INVALID
    )
    private String name;

    @NotBlank(message = BookConstant.INTRODUCE_TEXT_REQUIRED)
    @Size(max = 200, message = BookConstant.INTRODUCE_TEXT_MAX_LENGTH)
    private String introduceText;

    @NotBlank(message = BookConstant.DESCRIPTION_REQUIRED)
    @Size(max = 5000, message = BookConstant.DESCRIPTION_MAX_LENGTH)
    private String description;

    @NotBlank(message = BookConstant.READER_REQUIRED)
    @Size(max = 200, message = BookConstant.READER_MAX_LENGTH)
    private String reader;

    @NotNull(message = BookConstant.PRICE_REQUIRED)
    @DecimalMin(value = "0.0", message = BookConstant.PRICE_POSITIVE)
    private Double price;

    @NotNull(message = BookConstant.QUANTITY_REQUIRED)
    @Min(value = 1, message = BookConstant.QUANTITY_MIN)
    @Max(value = 1000, message = BookConstant.QUANTITY_MAX)
    private Integer quantity;

    @NotBlank(message = BookConstant.AUTHOR_REQUIRED)
    @Size(max = 100, message = BookConstant.AUTHOR_MAX_LENGTH)
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ'\\-\\.\\s]{1,100}$",
            message = BookConstant.AUTHOR_INVALID
    )
    private String author;

    private String sold;

    @NotBlank(message = CategoryConstant.NAME_REQUIRED)
    @Size(max = 100, message = CategoryConstant.NAME_MAX_LENGTH)
    @Pattern(
            regexp = "^[A-Za-zÀ-ỹà-ỹ0-9'\\-\\.\\:\\s]{1,100}$",
            message = CategoryConstant.NAME_INVALID
    )
    private String category;
}
