package com.goat.HireHub.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

public record  JobRequest(
                          @Size(max=100) @NotBlank(message = "Job title is required") String title,
                          @Size(max=1000) @NotBlank (message="job description is required") String description,
                          @Positive(message = "salary must be greater than zero") BigDecimal salary,
                          @NotNull Long companyId,
                          @NotBlank(message = "job url is required")
                          @URL(message = "must be a valid url")
                          String url,
                          @NotBlank(message = "job location is required")
                          @Size(max=100,message = "location cannot exceed 100 characters")
                          String location
                   ){ }
