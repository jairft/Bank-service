package com.accountservice.exception;

public class InvalidTransactionalPasswordException extends RuntimeException {
    public InvalidTransactionalPasswordException(String message) {
        super(message);
    }
}
