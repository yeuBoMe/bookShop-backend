package com.fullstack.bookShop.util.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum StatusEnum {
    SUCCESS,
    PENDING,
    CANCEL,
    SHIPPING,
    PAID,
    UNPAID;

    /**
     * Luat chuyen trang thai don hang. SUCCESS va CANCEL la trang thai ket thuc:
     * mo lai chung se lam lech ke toan ton kho vi ton kho da duoc hoan khi huy.
     */
    private static final Map<StatusEnum, Set<StatusEnum>> ALLOWED_TRANSITIONS = Map.of(
            PENDING, EnumSet.of(SHIPPING, PAID, CANCEL),
            UNPAID, EnumSet.of(PAID, CANCEL),
            PAID, EnumSet.of(SHIPPING, CANCEL),
            SHIPPING, EnumSet.of(SUCCESS, CANCEL),
            SUCCESS, EnumSet.noneOf(StatusEnum.class),
            CANCEL, EnumSet.noneOf(StatusEnum.class)
    );

    /** Giu nguyen trang thai cu luon hop le de FE gui lai cung gia tri khong bi tu choi. */
    public boolean canTransitionTo(StatusEnum target) {
        return this == target || ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    /**
     * Don o trang thai nay co dang giu ton kho hay khong.
     * Ton kho bi tru ngay luc tao don, nen chi don da huy la khong con giu.
     */
    public boolean holdsStock() {
        return this != CANCEL;
    }
}
