package com.goat.HireHub.user.dto;

import com.goat.HireHub.education.Education;
import java.time.LocalDate;

public record EducationResponse(
        Long id,
        String institutionName,
        String degreeType,
        String fieldOfStudy,
        LocalDate startDate,
        LocalDate endDate,
        Boolean isCurrent,
        String activitiesAndSocieties
) {
    public static EducationResponse from(Education education) {
        return new EducationResponse(
                education.getId(),
                education.getInstitutionName(),
                education.getDegreeType(),
                education.getFieldOfStudy(),
                education.getStartDate(),
                education.getEndDate(),
                education.isCurrent(),
                education.getActivitiesAndSocieties()
        );
    }
}
