package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum OrderEnum {
    ID_NOT_FOUND("Order not found with ID: %d");

    private final String message;

    OrderEnum(String message) {
        this.message = message;
    }
}
