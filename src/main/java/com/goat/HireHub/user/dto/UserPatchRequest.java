package com.goat.HireHub.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserPatchRequest(
        @Size(min=1,max=100,message = "first name must be between 1 and 100 characters")String firstName,
        @Size(min=1,max=100,message = "last name must be between 1 and 100 characters")String lastName,
        @Email(message="please enter a valid email address")
        @Size(min=1,max=100,message = "email must be between 1 and 100 characters")String email,
        @Size(min=1,max=100,message = "email must be between 1 and 100 characters")String username,
        @Size(min=8,max=72,message="password must be between 8 and 72 characters")String password

        ) {
}
