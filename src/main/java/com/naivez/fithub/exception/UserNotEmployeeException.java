package com.naivez.fithub.exception;

public class UserNotEmployeeException extends RuntimeException {

    public UserNotEmployeeException(String message) {
        super(message);
    }
}
