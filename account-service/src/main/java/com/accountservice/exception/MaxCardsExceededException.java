package com.accountservice.exception;

public class MaxCardsExceededException extends RuntimeException {
    public MaxCardsExceededException(String msg){ super(msg); }
}
