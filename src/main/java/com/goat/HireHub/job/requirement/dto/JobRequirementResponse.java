package com.goat.HireHub.job.requirement.dto;

import com.goat.HireHub.job.RequirementType;
import com.goat.HireHub.job.requirement.JobRequirement;

public record JobRequirementResponse(Long id, RequirementType requirementType, Integer position, String content) {
     public static JobRequirementResponse from(JobRequirement requirement){
          return new JobRequirementResponse(
                  requirement.getId(),
                  requirement.getRequirementType(),
                  requirement.getDisplayOrder(),
                  requirement.getContent()
                 );
     }
}
