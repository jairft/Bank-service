package com.authservice.exception;

public class InvalidPasswordException extends AuthServiceException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
