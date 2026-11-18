package com.goat.HireHub.security;

public record AuthenticatedUser(
        Long id,
        String email,
        String passwordHash
) {
}
