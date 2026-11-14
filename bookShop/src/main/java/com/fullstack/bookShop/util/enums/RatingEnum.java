package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum RatingEnum {
    ID_NOT_FOUND("rating.error.id-not-found"),
    USER_BOOK_NOT_FOUND("rating.error.user-book-not-found"),
    USER_BOOK_ALREADY_RATED("rating.error.user-book-already-rated");

    private final String messageKey;

    RatingEnum(String messageKey) {
        this.messageKey = messageKey;
    }
}
