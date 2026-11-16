package com.goat.HireHub.job.dto;

import com.goat.HireHub.job.EmploymentType;
import com.goat.HireHub.job.ExperienceLevel;
import com.goat.HireHub.job.WorkPlaceType;

import java.math.BigDecimal;
import java.time.Instant;

public record JobResponseSummary(
        Long id,
        String title,
        BigDecimal salary,
        Long companyId,
        String companyName,
        String location,
        EmploymentType employmentType,
        ExperienceLevel experienceLevel,
        WorkPlaceType workPlaceType,
        String url,
        Instant createdAt
) {
}
