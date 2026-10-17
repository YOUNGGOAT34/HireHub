package com.goat.HireHub.user.dto;

import java.util.List;

public record FullProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String username,
        String headline,
        String summary,
        String searchStatus,
        List<ExperienceResponse> experiences
//        List<EducationResponse> educations
) {}
