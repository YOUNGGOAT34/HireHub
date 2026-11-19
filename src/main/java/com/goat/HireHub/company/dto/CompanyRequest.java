package com.goat.HireHub.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyRequest(@NotBlank(message = "company name is required") @Size(min=1,max = 100,message = "company name has to be between 1 and 100 characters")
                             String name,
                             @Pattern(regexp = ".*\\S.*", message = "company description cannot be empty")
                             @Size(min=1,max = 1000,message = "company description has to be between 1 and 1000 characters") String description) {}

