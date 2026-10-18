package com.goat.HireHub.user.dto;

import com.goat.HireHub.user.SearchStatus;

public record ProfileSummaryResponse(
        Long id,
        String headline,
        String summary,
        SearchStatus searchStatus
) {}
