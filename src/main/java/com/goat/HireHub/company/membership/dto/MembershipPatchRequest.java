package com.goat.HireHub.company.membership.dto;

import com.goat.HireHub.company.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MembershipPatchRequest(
        @Positive(message = "user id must be greater tha n 0")
        Long userId,
        @Positive(message = "company id must be greater than 0")
        Long companyId,
        @NotNull(message = "role is required")
        Role role) {
}
