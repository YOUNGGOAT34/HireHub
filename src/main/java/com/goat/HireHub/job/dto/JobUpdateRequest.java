package com.goat.HireHub.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record  JobUpdateRequest(
        @Size(max=100) @NotBlank(message = "Job title is required") String title,
        @Size(max=1000) @NotBlank String description,
        @Positive(message = "salary must be greater than zero") BigDecimal salary

      ){ }


