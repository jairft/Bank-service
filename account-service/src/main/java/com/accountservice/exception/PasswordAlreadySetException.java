package com.accountservice.exception;

public class PasswordAlreadySetException extends RuntimeException {

    public PasswordAlreadySetException() {
        super();
    }

    public PasswordAlreadySetException(String message) {
        super(message);
    }

    public PasswordAlreadySetException(String message, Throwable cause) {
        super(message, cause);
    }
}
