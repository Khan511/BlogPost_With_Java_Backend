package com.example.demo.exception.customeExceptions;

public class FileUploadException extends RuntimeException {
    private final boolean sizeLimit;

    public FileUploadException(String message, boolean sizeLimit) {
        super(message);
        this.sizeLimit = sizeLimit;
    }

    public boolean isSizeLimit() {
        return sizeLimit;
    }

}
