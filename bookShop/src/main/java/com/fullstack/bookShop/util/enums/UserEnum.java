package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum UserEnum {
    ID_NOT_FOUND("User not found with ID: %d"),
    EMAIL_NOT_FOUND("User not found with email: '%s'"),
    EMAIL_TOKEN_NOT_FOUND("User not found with token and email: '%s'"),
    EMAIL_EXISTS("User email '%s' already exists"),
    PHONE_EXISTS("User phone '%s' already exists");

    private final String message;

    UserEnum(String message) {
        this.message = message;
    }
}
