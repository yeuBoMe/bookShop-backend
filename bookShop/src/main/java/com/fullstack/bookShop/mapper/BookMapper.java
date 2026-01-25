package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.BookResponse;
import com.fullstack.bookShop.model.Book;
import com.fullstack.bookShop.model.Category;
import com.fullstack.bookShop.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookMapper {

    private final RatingRepository ratingRepository;

    public BookResponse convertBookToCreateResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .name(book.getName())
                .category(convertCategory(book))
                .price(book.getPrice())
                .quantity(book.getQuantity())
                .images(book.getImages())
                .author(book.getAuthor())
                .introduceText(book.getIntroduceText())
                .description(book.getDescription())
                .reader(book.getReader())
                .createdAt(book.getCreatedAt())
                .createdBy(book.getCreatedBy())
                .build();
    }

    public BookResponse convertBookToUpdateResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .name(book.getName())
                .category(convertCategory(book))
                .price(book.getPrice())
                .quantity(book.getQuantity())
                .images(book.getImages())
                .author(book.getAuthor())
                .introduceText(book.getIntroduceText())
                .description(book.getDescription())
                .reader(book.getReader())
                .updatedAt(book.getUpdatedAt())
                .updatedBy(book.getUpdatedBy())
                .build();
    }

    public BookResponse convertBookToViewResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .name(book.getName())
                .category(convertCategory(book))
                .price(book.getPrice())
                .quantity(book.getQuantity())
                .images(book.getImages())
                .author(book.getAuthor())
                .introduceText(book.getIntroduceText())
                .description(book.getDescription())
                .reader(book.getReader())
                .averageStars(ratingRepository.getAverageRatingByBookId(book.getId()))
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .createdBy(book.getCreatedBy())
                .updatedBy(book.getUpdatedBy())
                .build();
    }

    private String convertCategory(Book book) {
        return Optional.ofNullable(book.getCategory())
                .map(Category::getName)
                .orElse(null);
    }
}
