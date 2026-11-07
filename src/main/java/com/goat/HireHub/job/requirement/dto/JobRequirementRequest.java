package com.goat.HireHub.job.requirement.dto;

import com.goat.HireHub.job.RequirementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JobRequirementRequest(
         @NotNull(message = "job requirement type is required")
         RequirementType requirementType,
         Integer position,
         @NotBlank(message = "job requirement content cannot be blank")
         String content
) {
}
