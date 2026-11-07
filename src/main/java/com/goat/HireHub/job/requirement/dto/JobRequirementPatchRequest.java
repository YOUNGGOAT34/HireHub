package com.goat.HireHub.job.requirement.dto;

import com.goat.HireHub.job.RequirementType;
import jakarta.validation.constraints.Size;

public record JobRequirementPatchRequest(
      RequirementType requirementType,
      Integer position,
      @Size(min=1,max=100,message ="description content must be between 1 and 100 characters")
      String content) {
}
