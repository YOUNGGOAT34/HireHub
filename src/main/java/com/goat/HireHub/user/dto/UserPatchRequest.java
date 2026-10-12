package com.goat.HireHub.user.dto;

import com.goat.HireHub.user.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserPatchRequest(
        @Size(min=1,max=100,message = "first name must be between 1 and 100 characters")String firstName,
        @Size(min=1,max=100,message = "last name must be between 1 and 100 characters")String lastName,
        @Email(message="please enter a valid email address")
        @Size(min=1,max=100,message = "email must be between 1 and 100 characters")String email

        ) {
}
