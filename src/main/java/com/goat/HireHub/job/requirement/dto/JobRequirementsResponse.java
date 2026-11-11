package com.goat.HireHub.job.requirement.dto;

import java.util.List;

public record JobRequirementsResponse(
        Long jobId,
        List<JobRequirementResponse> responsibilities,
        List<JobRequirementResponse> qualifications,
        List<JobRequirementResponse> preferredQualifications,
        List<JobRequirementResponse> niceToHave
) {
}
