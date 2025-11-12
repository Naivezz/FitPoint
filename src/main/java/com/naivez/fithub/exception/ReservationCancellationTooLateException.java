package com.naivez.fithub.exception;

public class ReservationCancellationTooLateException extends RuntimeException {

    public ReservationCancellationTooLateException(String message) {
        super(message);
    }
}
