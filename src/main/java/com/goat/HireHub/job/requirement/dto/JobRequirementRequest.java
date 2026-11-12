package com.goat.HireHub.job.requirement.dto;

import com.goat.HireHub.job.requirement.RequirementType;
import jakarta.validation.constraints.*;

public record JobRequirementRequest(
         @NotNull(message = "job requirement type is required")
         RequirementType requirementType,
         @Min(value = 1,message = "position must be greater than 0")
         Integer position,
         @NotBlank(message = "job requirement content cannot be blank")
         @Pattern(regexp = ".*\\S.*", message = "content cannot be blank")
         @Size(min=1,max=300,message ="description content must be between 1 and 300 characters")
         String content
) {
}
