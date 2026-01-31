package com.fullstack.bookShop.util.constant.entity;

public final class OrderConstant {

    private OrderConstant() {}

    public static final String NAME_REQUIRED = "Name is required";

    public static final String NAME_MAX_LENGTH = "Name must not exceed 100 characters";

    public static final String NAME_INVALID = "Name contains invalid characters";

    public static final String EMAIL_INVALID = "Email should be valid";

    public static final String EMAIL_MAX_LENGTH = "Email must not exceed 150 characters";

    public static final String PHONE_REQUIRED = "Phone number is required";

    public static final String PHONE_INVALID = "Phone number is invalid. Example: 0912345678 or +84912345678";

    public static final String ADDRESS_REQUIRED = "Address is required";

    public static final String ADDRESS_MAX_LENGTH = "Address must be less than 255 characters";

    public static final String ADDRESS_INVALID =
            "Address can only contain letters, numbers, spaces, and basic punctuation (.,-/#:')";

    public static final String PAYMENT_METHOD_REQUIRED = "Payment method is required";

    public static final String STATUS_REQUIRED = "Status is required";
}

