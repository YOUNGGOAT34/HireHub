package com.goat.HireHub.job;

public record JobRequirementRespose(Long id,RequirementType requirementType,Integer position,String content) {
     public static JobRequirementRespose from(JobRequirement requirement){
          return new JobRequirementRespose(
                  requirement.getId(),
                  requirement.getRequirementType(),
                  requirement.getDisplayOrder(),
                  requirement.getContent()
                 );
     }
}
