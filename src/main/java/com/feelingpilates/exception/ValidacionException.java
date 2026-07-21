package com.feelingpilates.exception;

/** Errores de negocio que no encajan en la validacion declarativa de Bean Validation. */
public class ValidacionException extends RuntimeException {

    public ValidacionException(String message) {
        super(message);
    }
}
