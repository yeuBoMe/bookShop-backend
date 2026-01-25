package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum PermissionEnum {
    ID_NOT_FOUND("Permission not found with ID: %d"),
    NAME_EXISTS("Permission name '%s' already exists"),
    API_EXISTS("Permission with API path '%s', method '%s' and module '%s' already exists");

    private final String message;

    PermissionEnum(String message) {
        this.message = message;
    }
}
