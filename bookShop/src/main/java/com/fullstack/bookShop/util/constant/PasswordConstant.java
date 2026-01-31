package com.fullstack.bookShop.util.constant;

public final class PasswordConstant {

    private PasswordConstant() {}

    public static final String CURRENT_NEW_PASSWORD_DUPLICATE =
            "New password must be different from the current password";

    public static final String NEW_CONFIRM_PASSWORD_NOT_MATCH =
            "Confirm password does not match the new password";

    public static final String PASSWORD_NOT_MATCH = "Current password ís incorrect";

    public static final String CURRENT_PASS_WORD_MIN_LENGTH = "Password must be at least 6 characters long!";

    public static final String NEW_PASSWORD_MIN_LENGTH = "New password must be at least 6 characters long!";

    public static final String CONFIRM_PASSWORD_MIN_LENGTH = "Confirm password must be at least 6 characters long!";

    public static final String DEFAULT_PASSWORD = "123456";
}
