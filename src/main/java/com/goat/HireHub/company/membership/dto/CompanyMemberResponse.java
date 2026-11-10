package com.goat.HireHub.company.membership.dto;

import com.goat.HireHub.company.Role;
import com.goat.HireHub.company.membership.CompanyMembership;

public record CompanyMemberResponse(
        Long userId,
        String firstName,
        String lastName,
        String email,
        Role role
) {
    public static CompanyMemberResponse from(CompanyMembership membership) {
        return new CompanyMemberResponse(
                membership.getUser().getId(),
                membership.getUser().getFirstName(),
                membership.getUser().getLastName(),
                membership.getUser().getEmail(),
                membership.getRole()
        );
    }
}