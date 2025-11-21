package com.kov.lifeauthmicroservice.exceptions;

public class PasswordsDoesntMatchException extends RuntimeException {
    public PasswordsDoesntMatchException(String message) {
        super(message);
    }
}
