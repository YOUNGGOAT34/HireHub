package com.goat.HireHub.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record EducationRequest(
        @NotBlank(message = "Institution name is required") String institutionName,
        String degreeType,
        String fieldOfStudy,
        @NotNull(message = "Start date is required") LocalDate startDate,
        LocalDate endDate,
        @NotNull(message = "isCurrent flag is required") Boolean isCurrent,
        String activitiesAndSocieties
) {
    public EducationRequest {
        if (isCurrent != null && isCurrent && endDate != null) {
            throw new IllegalArgumentException("An ongoing education timeline cannot contain an end date.");
        }
        if (isCurrent != null && !isCurrent && endDate == null) {
            throw new IllegalArgumentException("Completed education timelines must include an end date.");
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("The end date cannot be earlier than the start date.");
        }
    }
}
