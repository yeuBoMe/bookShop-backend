package com.fullstack.bookShop.exception.custom;

public class PermissionException extends Exception {

    public PermissionException() {
        super();
    }

    public PermissionException(String message) {
        super(message);
    }
}