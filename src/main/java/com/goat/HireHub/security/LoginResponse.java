package com.goat.HireHub.security;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn
) {}