package com.goat.HireHub.profile.dto;

import com.goat.HireHub.experience.dto.ExperienceResponse;
import com.goat.HireHub.user.SearchStatus;
import com.goat.HireHub.education.dto.EducationResponse;

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
