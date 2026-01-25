package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum RatingEnum {
    ID_NOT_FOUND("Rating not found with ID: %d"),
    USER_BOOK_NOT_FOUND("Rating not found with userId '%d' and bookId '%d'");

    private final String message;

    RatingEnum(String message) {
        this.message = message;
    }
}
