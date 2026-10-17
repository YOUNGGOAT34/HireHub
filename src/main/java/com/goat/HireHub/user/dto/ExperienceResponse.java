package com.goat.HireHub.user.dto;

import com.goat.HireHub.user.Experience;

import java.time.LocalDate;

public record ExperienceResponse(Long id,
                                 String jobTitle,
                                 Long companyId,
                                 String companyName,
                                 LocalDate startDate,
                                 LocalDate endDate,Boolean isCurrent,
                                 String description) {
     public static ExperienceResponse from(Experience experience){
          return new ExperienceResponse(experience.getId(),
                  experience.getJobTitle(),
                  (experience.getCompany()!=null)?experience.getCompany().getId():null,
                  (experience.getCompany()!=null)?experience.getCompany().getName():null,
                  experience.getStartDate(),
                  experience.getEndDate(),
                  experience.isCurrent(),
                  experience.getDescription()
          );
     }
}
