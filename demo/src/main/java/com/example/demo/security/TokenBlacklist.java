package com.example.demo.security;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class TokenBlacklist {
    // A thread-safe map to store blacklisted tokens (token string -> true)
    // Using ConcurrentHashMap ensures that multiple threads can safely add/check
    // tokens simultaneously.
    // It's thread-safe, meaning multiple requests (threads) can access it without
    // causing data corruption.
    // It's faster than Collections.synchronizedMap() in concurrent environments.
    private final ConcurrentHashMap<String, Boolean> blacklistedTokens = new ConcurrentHashMap<>();

    // Adds a token to the blacklist
    public void add(String token) {
        // The token is stored with a value of `true` just to mark its presence
        blacklistedTokens.put(token, true);
    }

    // Checks whether a token is in the blacklist
    public boolean contains(String accessToken) {
        // If the token exists in the map, it is considered blacklisted
        return blacklistedTokens.containsKey(accessToken);
    }

}
