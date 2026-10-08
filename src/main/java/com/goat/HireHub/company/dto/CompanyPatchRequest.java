package com.goat.HireHub.company.dto;

import jakarta.validation.constraints.Size;

public record CompanyPatchRequest(@Size(min = 1, max = 100) String name, @Size(max = 1000) String description) {}
