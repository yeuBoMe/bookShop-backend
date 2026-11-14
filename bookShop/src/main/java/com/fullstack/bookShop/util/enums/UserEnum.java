package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum UserEnum {
    ID_NOT_FOUND("user.error.id-not-found"),
    EMAIL_NOT_FOUND("user.error.email-not-found"),
    EMAIL_TOKEN_NOT_FOUND("user.error.email-token-not-found"),
    EMAIL_EXISTS("user.error.email-exists"),
    PHONE_EXISTS("user.error.phone-exists");

    private final String messageKey;

    UserEnum(String messageKey) {
        this.messageKey = messageKey;
    }
}
