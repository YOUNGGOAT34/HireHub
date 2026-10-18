package com.goat.HireHub.user.dto;

import com.goat.HireHub.user.SearchStatus;

import java.util.List;

public record FullProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String username,
        String headline,
        String summary,
        SearchStatus searchStatus,
        List<ExperienceResponse> experiences,
        List<EducationResponse> educations
) {}
