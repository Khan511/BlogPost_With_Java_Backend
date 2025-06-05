package com.example.demo.exception.customeExceptions;

public class MailSendException extends RuntimeException {

    public MailSendException() {
        super("Filed to send email");
    }

    public MailSendException(String message) {
        super(message);
    }

}
