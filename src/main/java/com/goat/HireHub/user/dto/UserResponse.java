package com.goat.HireHub.user.dto;

import com.goat.HireHub.user.Role;
import com.goat.HireHub.user.User;

public record UserResponse(Long id, String firstName, String lastName, String email, Role role,String username) {
    public static UserResponse from(User user){
         return new UserResponse(user.getId(),user.getFirstName(),user.getLastName(),user.getEmail(),user.getRole(),user.getUsername());
    }
}
