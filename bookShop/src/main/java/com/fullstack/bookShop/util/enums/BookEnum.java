package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum BookEnum {
    ID_NOT_FOUND("Book not found with ID: %d"),
    NAME_AUTHOR_EXISTS("Book name '%s', author '%s' already exists");

    private final String message;

    BookEnum(String message) {
        this.message = message;
    }
}
