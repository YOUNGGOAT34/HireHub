package com.goat.HireHub.company.dto;

import com.goat.HireHub.company.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberInviteRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Enter a valid email address")
        String email,

        @NotNull(message = "Role is required")
        Role role
) {}