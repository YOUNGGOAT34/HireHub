package com.goat.HireHub.job.dto;

import com.goat.HireHub.job.WorkPlaceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

public record JobPatchRequest(
        @Size(min=1,max=100,message = "title must be between 1 and 100 characters") String title,
        @Pattern(regexp = ".*\\S.*", message = "description cannot be empty or whitespace if provided")
        @Size(max=1000,message = "description must be between 1 and 1000 characters") String description,
        @Positive(message = "salary must be greater than zero") BigDecimal salary,
        @URL(message = "must be a valid url")
        @Pattern(regexp = ".*\\S.*", message = "URL cannot be empty or whitespace if provided")
        String url,
        @Pattern(regexp = ".*\\S.*", message = "location cannot be empty or whitespace if provided")
        String location,
        WorkPlaceType workPlaceType
) { }
