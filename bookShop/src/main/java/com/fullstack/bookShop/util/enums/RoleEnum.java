package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    ID_NOT_FOUND("Role not found with ID: %d"),
    NAME_NOT_FOUND("Role not found with name: '%s'"),
    NAME_EXISTS("Role name '%s' already exists");

    private final String message;

    RoleEnum(String message) {
        this.message = message;
    }
}
