package com.goat.HireHub.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message="first name is required") @Size(max = 100,message = "first name must not exceed 100 characters")String firstName,
        @NotBlank(message="email is required") @Size(max=100) @Email(message="please provide a valid email") String email,
        @NotBlank (message="last name is required") @Size(max=100,message = "last name must not exceed 100 characters") String lastName,
        @Size(max=100,message = "username must not exceed 100 characters") String username,
        @NotBlank (message="password is required") @Size(min=8,max=72,message="password must be between 8 and 72 characters")String password
  ) {
    @Override
    public String toString() {
        return "UserRequest[email=" + email + "]";
    }
}
