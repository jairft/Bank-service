package com.authservice.exception;


public class InvalidCredentialsException extends AuthServiceException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
