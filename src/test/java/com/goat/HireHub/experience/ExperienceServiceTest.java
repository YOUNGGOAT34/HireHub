package com.goat.HireHub.experience;

import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.experience.dto.ExperienceRequest;
import com.goat.HireHub.experience.dto.ExperienceResponse;
import com.goat.HireHub.profile.CandidateProfile;
import com.goat.HireHub.profile.CandidateProfileService;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExperienceServiceTest {

    @Mock
    ExperienceRepository experienceRepository;
    @Mock
    SecurityUtils securityUtils;
    @Mock
    CandidateProfileService candidateProfileService;

    @InjectMocks
    ExperienceService experienceService;


    @Test
    void addExperience_attachesToCurrentUsersProfileAndCopiesFields() {
        CandidateProfile myProfile = profileWithId(1L);
        User me = userWithProfile(myProfile);
        when(securityUtils.getCurrentUser()).thenReturn(me);
        when(candidateProfileService.getOrCreateCandidateProfile(me)).thenReturn(myProfile);
        when(experienceRepository.save(any(Experience.class))).thenAnswer(inv -> inv.getArgument(0));

        experienceService.addExperience(request("Backend Dev"));

        ArgumentCaptor<Experience> captor = ArgumentCaptor.forClass(Experience.class);
        verify(experienceRepository).save(captor.capture());
        assertEquals("Backend Dev", captor.getValue().getJobTitle());
        assertSame(myProfile, captor.getValue().getCandidateProfile());
    }

    // ---------- update ----------

    @Test
    void updateExperience_whenOwner_changesFields() {
        CandidateProfile myProfile = profileWithId(1L);
        when(securityUtils.getCurrentUser()).thenReturn(userWithProfile(myProfile));
        Experience existing = experienceOwnedBy(myProfile, "Old Title");
        when(experienceRepository.findById(10L)).thenReturn(Optional.of(existing));

        experienceService.updateExperience(10L, request("New Title"));

        assertEquals("New Title", existing.getJobTitle());
    }

    @Test
    void updateExperience_whenSomeoneElsesRecord_throwsAccessDeniedAndChangesNothing() {
        CandidateProfile myProfile = profileWithId(1L);
        CandidateProfile otherProfile = profileWithId(2L);
        when(securityUtils.getCurrentUser()).thenReturn(userWithProfile(myProfile));
        Experience theirs = experienceOwnedBy(otherProfile, "Their Title");
        when(experienceRepository.findById(10L)).thenReturn(Optional.of(theirs));

        assertThrows(AccessDeniedException.class,
                () -> experienceService.updateExperience(10L, request("Hacked")));

        assertEquals("Their Title", theirs.getJobTitle());
    }

    @Test
    void updateExperience_whenIdMissing_throwsNotFound() {
        when(experienceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> experienceService.updateExperience(99L, request("x")));

        verifyNoInteractions(securityUtils);   // we never even checked who's asking
    }

    // ---------- delete ----------

    @Test
    void deleteExperienceById_whenOwner_deletesIt() {
        CandidateProfile myProfile = profileWithId(1L);
        when(securityUtils.getCurrentUser()).thenReturn(userWithProfile(myProfile));
        Experience mine = experienceOwnedBy(myProfile, "Mine");
        when(experienceRepository.findById(10L)).thenReturn(Optional.of(mine));

        experienceService.deleteExperienceById(10L);

        verify(experienceRepository).delete(mine);
    }

    @Test
    void deleteExperienceById_whenSomeoneElsesRecord_throwsAccessDeniedAndDeletesNothing() {
        when(securityUtils.getCurrentUser()).thenReturn(userWithProfile(profileWithId(1L)));
        Experience theirs = experienceOwnedBy(profileWithId(2L), "Theirs");
        when(experienceRepository.findById(10L)).thenReturn(Optional.of(theirs));

        assertThrows(AccessDeniedException.class, () -> experienceService.deleteExperienceById(10L));

        verify(experienceRepository, never()).delete(any(Experience.class));
    }

    @Test
    void deleteExperienceById_whenIdMissing_throwsNotFound() {
        when(experienceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> experienceService.deleteExperienceById(99L));

        verify(experienceRepository, never()).delete(any(Experience.class));
    }

    // ---------- the bug: user has no profile ----------

    @Test
    void deleteExperienceById_whenCurrentUserHasNoProfile_throwsAccessDenied_notNpe() {
        User noProfileUser = new User();
        noProfileUser.setId(1L);
        when(securityUtils.getCurrentUser()).thenReturn(noProfileUser);
        Experience theirs = experienceOwnedBy(profileWithId(2L), "Theirs");
        when(experienceRepository.findById(10L)).thenReturn(Optional.of(theirs));
        assertThrows(AccessDeniedException.class, () -> experienceService.deleteExperienceById(10L));
    }


    @Test
    void getAllExperiencesByUserId_returnsNewestFirst() {
        CandidateProfile profile = profileWithId(1L);
        Experience older = experienceOwnedBy(profile, "Older");
        older.setStartDate(LocalDate.of(2020, 1, 1));
        Experience newer = experienceOwnedBy(profile, "Newer");
        newer.setStartDate(LocalDate.of(2023, 1, 1));
        when(experienceRepository.findByCandidateProfileId(1L)).thenReturn(List.of(older, newer));

        List<ExperienceResponse> result = experienceService.getAllExperiencesByUserId(1L);

        assertEquals("Newer", result.get(0).jobTitle());
        assertEquals("Older", result.get(1).jobTitle());
    }

    // ---------- helpers ----------

    private CandidateProfile profileWithId(Long id) {
        CandidateProfile p = new CandidateProfile();
        p.setId(id);
        return p;
    }

    private User userWithProfile(CandidateProfile profile) {
        User u = new User();
        u.setCandidateProfile(profile);
        return u;
    }

    private Experience experienceOwnedBy(CandidateProfile profile, String title) {
        Experience e = new Experience();
        e.setJobTitle(title);
        e.setStartDate(LocalDate.of(2022, 1, 1));
        e.setCandidateProfile(profile);
        return e;
    }

    private ExperienceRequest request(String title) {
        return new ExperienceRequest(title,"high throughtput and low latency backend services",2L,"amazon",
                LocalDate.of(2027,1,1),null,true);
    }
}
