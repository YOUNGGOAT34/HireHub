package com.goat.HireHub.security;

import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("No authenticated user found in session"));
    }
}