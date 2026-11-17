package com.goat.HireHub.company.membership.dto;

import com.goat.HireHub.company.Role;
import com.goat.HireHub.company.membership.CompanyMembership;

public record CompanyMemberResponse(
        Long userId,
        String firstName,
        String lastName,
        Role role
) {}