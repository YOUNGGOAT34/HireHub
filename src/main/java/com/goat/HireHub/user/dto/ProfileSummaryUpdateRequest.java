package com.goat.HireHub.user.dto;

import com.goat.HireHub.user.SearchStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfileSummaryUpdateRequest(
        @Size(max = 120, message = "Headline cannot exceed 120 characters")
        String headline,

        @Size(max = 5000, message = "Summary cannot exceed 5000 characters")
        String summary,

        @NotNull(message = "Search status is required")
        SearchStatus searchStatus
) {}