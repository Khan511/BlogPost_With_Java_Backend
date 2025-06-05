package com.example.demo.exception.customeExceptions;

public class DataIntegrityViolationException extends RuntimeException {

    public DataIntegrityViolationException() {
        super("Database constraint violation");
    }

    public DataIntegrityViolationException(String message) {
        super(message);
    }

}
