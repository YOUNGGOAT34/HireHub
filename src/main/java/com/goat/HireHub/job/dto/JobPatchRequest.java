package com.goat.HireHub.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record JobPatchRequest(
        @Size(max=100) String title,
        @Size(max=1000) String description,
        @Positive(message = "salary must be greater than zero") BigDecimal salary
) { }
