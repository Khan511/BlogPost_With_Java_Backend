package com.example.demo.exception.customeExceptions;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException() {
        super("Access Denied, INsufficient permissions");
    }

    public AccessDeniedException(String messsage) {
        super(messsage);
    }

}
