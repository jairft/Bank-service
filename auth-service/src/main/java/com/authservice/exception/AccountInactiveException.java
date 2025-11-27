package com.authservice.exception;

public class AccountInactiveException extends AuthServiceException {
    public AccountInactiveException(String message) {
        super(message);
    }
}
