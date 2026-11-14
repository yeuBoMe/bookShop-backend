package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    ID_NOT_FOUND("role.error.id-not-found"),
    NAME_NOT_FOUND("role.error.name-not-found"),
    NAME_EXISTS("role.error.name-exists");

    private final String messageKey;

    RoleEnum(String messageKey) {
        this.messageKey = messageKey;
    }
}
