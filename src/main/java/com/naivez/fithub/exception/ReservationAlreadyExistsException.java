package com.naivez.fithub.exception;

public class ReservationAlreadyExistsException extends RuntimeException {

    public ReservationAlreadyExistsException(String message) {
        super(message);
    }
}
