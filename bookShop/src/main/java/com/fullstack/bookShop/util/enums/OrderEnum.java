package com.fullstack.bookShop.util.enums;

import lombok.Getter;

@Getter
public enum OrderEnum {
    ID_NOT_FOUND("order.error.id-not-found"),
    DETAIL_REQUIRED("order.error.detail-required"),
    INVALID_STATUS_TRANSITION("order.error.invalid-status-transition");

    private final String messageKey;

    OrderEnum(String messageKey) {
        this.messageKey = messageKey;
    }
}
