package com.fullstack.bookShop.util.exception.custom;

public class PermissionException extends Exception {

    public PermissionException() {
        super();
    }

    public PermissionException(String message) {
        super(message);
    }
}