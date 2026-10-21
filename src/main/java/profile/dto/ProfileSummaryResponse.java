package profile.dto;

import com.goat.HireHub.user.SearchStatus;

public record ProfileSummaryResponse(
        Long id,
        String headline,
        String summary,
        SearchStatus searchStatus
) {}
