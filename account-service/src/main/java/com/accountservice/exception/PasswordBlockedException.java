package com.accountservice.exception;

public class PasswordBlockedException extends RuntimeException {
    public PasswordBlockedException(String message) {
        super(message);
    }
}
