package com.feelingpilates.auth;

public class GoogleLoginDisabledException extends RuntimeException {

    public GoogleLoginDisabledException(String message) {
        super(message);
    }
}
