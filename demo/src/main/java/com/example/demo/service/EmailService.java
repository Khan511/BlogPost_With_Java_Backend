package com.example.demo.service;

public interface EmailService {
    void sendNewAccountEmail(String name, String email, String token);
}
