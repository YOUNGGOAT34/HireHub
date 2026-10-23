package com.goat.HireHub.education.dto;

import com.goat.HireHub.exception.BadRequestException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EducationRequest(
        @NotBlank(message = "Institution name is required") @Size(max= 255,message = "institution name characters cannot exceed 255")
        String institutionName,
        String degreeType,
        String fieldOfStudy,
        @NotNull(message = "Start date is required") LocalDate startDate,
        LocalDate endDate,
        @NotNull(message = "isCurrent flag is required") Boolean isCurrent,
        String activitiesAndSocieties
) {
    public EducationRequest {
        if (isCurrent != null && isCurrent && endDate != null) {
            throw new BadRequestException("An ongoing education timeline cannot contain an end date.");
        }
        if (isCurrent != null && !isCurrent && endDate == null) {
            throw new BadRequestException("Completed education timelines must include an end date.");
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("The end date cannot be earlier than the start date.");
        }
    }
}
