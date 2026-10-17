package com.goat.HireHub.user.dto;

public record ProfileSummaryResponse(
        Long id,
        String headline,
        String summary,
        String searchStatus
) {}
