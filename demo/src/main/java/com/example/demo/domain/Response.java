package com.example.demo.domain;

import java.util.Map;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

// This annotation tells Jackson to exclude fields with default values (e.g., null or 0) during JSON serialization
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record Response(String time, int code, String path, HttpStatus status, String message, String exception,
                Map<?, ?> data) {

}
