package com.goat.HireHub.company.dto;

import jakarta.validation.constraints.Size;

public record CompanyPatchRequest(@Size(min = 1, max = 100,message = "company name has to be between 1 and 100 characters")
                                  String name,
                                  @Size(min=1,max = 1000,message = "company description has to be between 1 and 1000 characters")
                                  String description
) {}
