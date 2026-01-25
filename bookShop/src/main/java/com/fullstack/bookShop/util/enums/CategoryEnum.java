package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum CategoryEnum {
    ID_NOT_FOUND("Category not found with ID: %d"),
    NAME_EXISTS("Category name '%s' already exists");

    private final String message;

    CategoryEnum(String message) {
        this.message = message;
    }
}

