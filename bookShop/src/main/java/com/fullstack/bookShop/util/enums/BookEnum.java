package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum BookEnum {
    ID_NOT_FOUND("book.error.id-not-found"),
    NAME_AUTHOR_EXISTS("book.error.name-author-exists"),
    QUANTITY_NOT_ENOUGH("book.error.quantity-not-enough"),
    QUANTITY_INVALID("book.error.quantity-invalid"),
    REFERENCED_BY_ORDER("book.error.referenced-by-order"),
    NAME_AUTHOR_NOT_FOUND("book.error.name-author-not-found");

    private final String messageKey;

    BookEnum(String messageKey) {
        this.messageKey = messageKey;
    }
}
