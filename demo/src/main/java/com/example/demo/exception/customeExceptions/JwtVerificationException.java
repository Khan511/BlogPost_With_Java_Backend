package com.example.demo.exception.customeExceptions;

public class JwtVerificationException extends RuntimeException {
    public JwtVerificationException() {
        super("Token validation failed");
    }

    public JwtVerificationException(String messaget) {
        super(messaget);
    }
}
