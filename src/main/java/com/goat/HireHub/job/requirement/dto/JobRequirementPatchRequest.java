package com.goat.HireHub.job.requirement.dto;

import com.goat.HireHub.job.RequirementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record JobRequirementPatchRequest(
      RequirementType requirementType,
      @Min(value = 1,message = "position must be greater than zero")
      Integer position,
      @Size(min=1,max=300,message ="description content must be between 1 and 300 characters")
      @Pattern(regexp = ".*\\S.*", message = "content cannot be blank")
      String content) {
}
