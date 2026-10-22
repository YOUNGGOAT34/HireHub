package com.goat.HireHub.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record JobPatchRequest(
        @Size(min=1,max=100,message = "title must be between 1 and 100 characters") String title,
        @Size(min=1,max=1000,message = "description must be between 1 and 1000 characters") String description,
        @Positive(message = "salary must be greater than zero") BigDecimal salary
) { }
