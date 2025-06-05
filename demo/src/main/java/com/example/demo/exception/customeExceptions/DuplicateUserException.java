package com.example.demo.exception.customeExceptions;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException() {
        super("User already exist");
    }

    public DuplicateUserException(String message) {
        super(message);
    }

}
