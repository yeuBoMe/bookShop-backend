package com.fullstack.bookShop.util.constant.entity;

public final class BookConstant {

    private BookConstant() {}

    public static final String NAME_REQUIRED = "Book's name is required";

    public static final String NAME_MAX_LENGTH = "Book's name must not exceed 100 characters";

    public static final String NAME_INVALID = "Book's name contains invalid characters";

    public static final String INTRODUCE_TEXT_REQUIRED = "Introduce text is required";

    public static final String INTRODUCE_TEXT_MAX_LENGTH = "Introduce text must not exceed 200 characters";

    public static final String DESCRIPTION_REQUIRED = "Description is required";

    public static final String DESCRIPTION_MAX_LENGTH = "Description must not exceed 5000 characters";

    public static final String READER_REQUIRED = "Reader is required";

    public static final String READER_MAX_LENGTH = "Reader must not exceed 200 characters";

    public static final String PRICE_REQUIRED = "Price is required";

    public static final String PRICE_POSITIVE = "Price must be a positive number";

    public static final String QUANTITY_REQUIRED = "Quantity is required";

    public static final String QUANTITY_MIN = "Quantity must be at least 1";

    public static final String QUANTITY_MAX = "Quantity must not exceed 1000";

    public static final String AUTHOR_REQUIRED = "Author is required";

    public static final String AUTHOR_MAX_LENGTH = "Author must not exceed 100 characters";

    public static final String AUTHOR_INVALID = "Author contains invalid characters";
}
