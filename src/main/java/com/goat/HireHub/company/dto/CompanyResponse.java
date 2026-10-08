package com.goat.HireHub.company.dto;

import com.goat.HireHub.company.Company;

public record CompanyResponse(Long id, String name, String description) {
    public static CompanyResponse from(Company c) {
        return new CompanyResponse(c.getId(), c.getName(), c.getDescription());
    }
}