package com.goat.HireHub.experience.dto;

import com.goat.HireHub.exception.BadRequestException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/*
* end date can be null because the user might be currently working in this company
* start date is required
* job title is required
* company id can be null because it might not be a registred company on hirehub
* isCurrent flag is required
*  */

public record ExperienceRequest(
         @NotBlank(message = "Job title is required") String jobTitle,
         @NotBlank (message = "experience description is required")
         @Size(min=5,max=200) String description,
         @Positive(message = "a valid company id must be greater than 0") Long companyId,
         String companyName,
         @NotNull(message = "start date is required")
         LocalDate startDate,
         LocalDate endDate,
         @NotNull(message = "is current flag is required")Boolean isCurrent

) {

    public ExperienceRequest{
          if(isCurrent!=null && isCurrent && endDate!=null){
              throw new BadRequestException("an experience marked as current cannot have an end date");
          }

          if(isCurrent!=null && !isCurrent && endDate==null){
              throw new BadRequestException("a past experience must include an end date");
          }

          if (startDate !=null && endDate !=null && endDate.isBefore(startDate)){
              throw new BadRequestException("the end date cannot be earlier than the start date");
          }

          if (companyId==null && companyName==null){
              throw new BadRequestException("both company name and company id cannot be null");
          }
    }
}
