package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum CategoryEnum {
    ID_NOT_FOUND("category.error.id-not-found"),
    NAME_EXISTS("category.error.name-exists"),
    NAME_NOT_FOUND("category.error.name-not-found");

    private final String messageKey;

    CategoryEnum(String messageKey) {
        this.messageKey = messageKey;
    }
}

