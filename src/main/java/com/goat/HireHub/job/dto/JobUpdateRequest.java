package com.goat.HireHub.job.dto;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

public record  JobUpdateRequest(
        @Size(max=100) @NotBlank(message = "Job title is required") String title,
        @Size(max=1000) @NotBlank String description,
        @NotNull(message = "job salary is required")
        @Positive(message = "salary must be greater than zero") BigDecimal salary,
        @NotNull(message = "job url is required")
        @Pattern(regexp = ".*\\S.*", message = "URL cannot be empty or whitespace if provided")
        @URL(message = "must be a valid url")
        String url,
        @NotBlank(message = "job location is required")
        @Pattern(regexp = ".*\\S.*", message = "location cannot be empty or whitespace if provided")
        String location
      ){ }
