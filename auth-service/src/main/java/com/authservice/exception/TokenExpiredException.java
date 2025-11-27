package com.authservice.exception;

public class TokenExpiredException extends AuthServiceException {
    public TokenExpiredException(String message) {
        super(message);
    }
}
