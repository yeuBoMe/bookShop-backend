package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.CategoryResponse;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category, String methodName) {
        CategoryResponse.CategoryResponseBuilder builder = CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName());

        if (ActionConstant.CREATE.equals(methodName)) {
            builder.createdAt(category.getCreatedAt())
                    .createdBy(category.getCreatedBy());
        } else if (ActionConstant.UPDATE.equals(methodName)) {
            builder.updatedAt(category.getUpdatedAt())
                    .updatedBy(category.getUpdatedBy());
        } else if (ActionConstant.FETCH.equals(methodName)) {
            builder.createdAt(category.getCreatedAt())
                    .updatedAt(category.getUpdatedAt())
                    .createdBy(category.getCreatedBy())
                    .updatedBy(category.getUpdatedBy());
        }

        return builder.build();
    }
}
