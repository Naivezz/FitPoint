package com.naivez.fithub.exception;

public class CouponAlreadyExistsException extends RuntimeException {

    public CouponAlreadyExistsException(String message) {
        super(message);
    }
}
