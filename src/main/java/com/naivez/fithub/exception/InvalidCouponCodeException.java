package com.naivez.fithub.exception;

public class InvalidCouponCodeException extends RuntimeException {

    public InvalidCouponCodeException(String message) {
        super(message);
    }
}
