package com.kov.lifeauthmicroservice.exceptions;

public class UserNotRegisterException extends RuntimeException {
    public UserNotRegisterException(String message) {
        super(message);
    }
}
