package com.accountservice.exception;

public class PasswordNotSetException extends RuntimeException {
    public PasswordNotSetException(String message) {
        super(message);
    }
}
