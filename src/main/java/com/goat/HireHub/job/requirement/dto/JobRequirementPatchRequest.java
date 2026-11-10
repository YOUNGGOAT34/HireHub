package com.goat.HireHub.job.requirement.dto;

import com.goat.HireHub.job.RequirementType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record JobRequirementPatchRequest(
      RequirementType requirementType,
      Integer position,
      @Size(min=1,max=300,message ="description content must be between 1 and 300 characters")
      @Pattern(regexp = ".*\\S.*", message = "content cannot be blank")
      String content) {
}
