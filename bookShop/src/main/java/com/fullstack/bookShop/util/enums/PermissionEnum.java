package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum PermissionEnum {
    ID_NOT_FOUND("permission.error.id-not-found"),
    IDS_NOT_FOUND("permission.error.ids-not-found"),
    NAME_EXISTS("permission.error.name-exists"),
    API_EXISTS("permission.error.api-exists");

    private final String messageKey;

    PermissionEnum(String messageKey) {
        this.messageKey = messageKey;
    }
}
