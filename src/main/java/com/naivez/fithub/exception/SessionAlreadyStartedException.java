package com.naivez.fithub.exception;

public class SessionAlreadyStartedException extends RuntimeException {

    public SessionAlreadyStartedException(String message) {
        super(message);
    }
}
