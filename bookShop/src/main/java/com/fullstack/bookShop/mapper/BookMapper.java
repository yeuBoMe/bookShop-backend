package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BookMapper {

    public BookResponse toResponse(Book book, String methodName, Double... averageStars) {
        BookResponse.BookResponseBuilder builder = BookResponse.builder()
                .id(book.getId())
                .name(book.getName())
                .category(convertCategory(book))
                .price(book.getPrice())
                .quantity(book.getQuantity())
                .images(book.getImages())
                .author(book.getAuthor())
                .introduceText(book.getIntroduceText())
                .description(book.getDescription())
                .reader(book.getReader());

        if (ActionConstant.CREATE.equals(methodName)) {
            builder.createdAt(book.getCreatedAt())
                    .createdBy(book.getCreatedBy());
        } else if (ActionConstant.UPDATE.equals(methodName)) {
            builder.updatedAt(book.getUpdatedAt())
                    .updatedBy(book.getUpdatedBy());
        } else if (ActionConstant.FETCH.equals(methodName)) {
            builder.averageStars(
                            averageStars.length > 0
                                    ? averageStars[0]
                                    : 0.0
                    )
                    .createdAt(book.getCreatedAt())
                    .updatedAt(book.getUpdatedAt())
                    .createdBy(book.getCreatedBy())
                    .updatedBy(book.getUpdatedBy());
        }

        return builder.build();
    }

    public String convertCategory(Book book) {
        return Optional.ofNullable(book.getCategory())
                .map(Category::getName)
                .orElse(null);
    }
}
