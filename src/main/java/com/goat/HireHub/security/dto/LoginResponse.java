
package com.goat.HireHub.security.dto;

public record LoginResponse(
        String token,
        String tokenType,
        long expiresIn
) {}