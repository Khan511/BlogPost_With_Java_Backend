package com.example.demo.exception.customeExceptions;

public class RateLimitExceededException extends RuntimeException {

    private final int limit;

    public RateLimitExceededException(int limit) {
        super("Rate limit of " + limit + " exceede");
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }
}
