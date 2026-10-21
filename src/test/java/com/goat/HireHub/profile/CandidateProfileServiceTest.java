package com.goat.HireHub.profile;

import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.profile.dto.FullProfileResponse;
import com.goat.HireHub.profile.dto.ProfileSummaryResponse;
import com.goat.HireHub.profile.dto.ProfileSummaryUpdateRequest;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.SearchStatus;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandidateProfileServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    SecurityUtils securityUtils;
    @Mock CandidateProfileRepository candidateProfileRepository;

    @InjectMocks
    CandidateProfileService service;

    // ---------- getOrCreate ----------

    @Test
    void getOrCreate_whenProfileExists_returnsItAndSavesNothing() {
        User me = userWithId(1L);
        CandidateProfile existing = new CandidateProfile();
        when(candidateProfileRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertSame(existing, service.getOrCreateCandidateProfile(me));
        verify(candidateProfileRepository, never()).save(any(CandidateProfile.class));
    }

    @Test
    void getOrCreate_whenNone_createsProfileLinkedToUserWithDefaults() {
        User me = userWithId(1L);
        when(candidateProfileRepository.findById(1L)).thenReturn(Optional.empty());
        when(candidateProfileRepository.save(any(CandidateProfile.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        CandidateProfile result = service.getOrCreateCandidateProfile(me);

        assertSame(me, result.getUser());
        assertEquals(SearchStatus.OPEN_TO_WORK, result.getSearchStatus());
    }

    // ---------- updateMainProfile ----------

    @Test
    void updateMainProfile_whenProfileExists_savesHeadlineSummaryAndStatusSeparately() {
        User me = userWithId(1L);
        CandidateProfile existing = new CandidateProfile();
        existing.setId(1L);
        when(securityUtils.getCurrentUser()).thenReturn(me);
        when(candidateProfileRepository.findById(1L)).thenReturn(Optional.of(existing));

        ProfileSummaryResponse response = service.updateMainProfile(
                new ProfileSummaryUpdateRequest("Java Dev", "I build APIs", SearchStatus.OPEN_TO_WORK));

        // regression test for the old bug where summary was set to the headline
        assertEquals("Java Dev", existing.getHeadline());
        assertEquals("I build APIs", existing.getSummary());
        assertEquals("I build APIs", response.summary());
    }

    @Test
    void updateMainProfile_whenFirstTime_createsProfileAndReturnsItsId() {
        User me = userWithId(7L);
        when(securityUtils.getCurrentUser()).thenReturn(me);
        when(candidateProfileRepository.findById(7L)).thenReturn(Optional.empty());
        when(candidateProfileRepository.save(any(CandidateProfile.class))).thenAnswer(inv -> {
            CandidateProfile p = inv.getArgument(0);
            p.setId(7L);                       // what @MapsId does in the real database
            return p;
        });

        ProfileSummaryResponse response = service.updateMainProfile(
                new ProfileSummaryUpdateRequest("Dev", "About me", SearchStatus.OPEN_TO_WORK));

        assertEquals(7L, response.id());
        assertEquals("Dev", response.headline());
    }

    // ---------- getFullProfileByUserId ----------

    @Test
    void getFullProfile_whenUserMissing_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getFullProfileByUserId(99L));
    }

    @Test
    void getFullProfile_whenUserHasNoProfile_returnsUserInfoWithEmptyLists() {
        User me = userWithId(1L);
        me.setFirstName("Kim");
        when(userRepository.findById(1L)).thenReturn(Optional.of(me));

        FullProfileResponse result = service.getFullProfileByUserId(1L);

        assertEquals("Kim", result.firstName());
        assertNull(result.headline());
        assertTrue(result.experiences().isEmpty());
        assertTrue(result.educations().isEmpty());
    }

    private User userWithId(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }
}
