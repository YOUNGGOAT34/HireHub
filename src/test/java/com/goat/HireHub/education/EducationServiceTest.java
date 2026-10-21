package com.goat.HireHub.education;

import com.goat.HireHub.education.dto.EducationRequest;
import com.goat.HireHub.education.dto.EducationResponse;
import com.goat.HireHub.exception.ResourceNotFoundException;
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
class EducationServiceTest {

    @Mock
    EducationRepository educationRepository;
    @Mock
    SecurityUtils securityUtils;
    @Mock
    CandidateProfileService candidateProfileService;

    @InjectMocks
    EducationService educationService;


    @Test
    void addEducation_attachesToCurrentUsersProfileAndCopiesFields() {
        CandidateProfile myProfile = profileWithId(1L);
        User me = userWithProfile(myProfile);
        when(securityUtils.getCurrentUser()).thenReturn(me);
        when(candidateProfileService.getOrCreateCandidateProfile(me)).thenReturn(myProfile);
        when(educationRepository.save(any(Education.class))).thenAnswer(inv -> inv.getArgument(0));

        educationService.addEducation(request("University of Nairobi"));

        ArgumentCaptor<Education> captor = ArgumentCaptor.forClass(Education.class);
        verify(educationRepository).save(captor.capture());
        assertEquals("University of Nairobi", captor.getValue().getInstitutionName());
        assertSame(myProfile, captor.getValue().getCandidateProfile());
    }

    // ---------- update ----------

    @Test
    void updateEducation_whenOwner_changesFields() {
        CandidateProfile myProfile = profileWithId(1L);
        when(securityUtils.getCurrentUser()).thenReturn(userWithProfile(myProfile));
        Education existing = educationOwnedBy(myProfile, "Old School");
        when(educationRepository.findById(10L)).thenReturn(Optional.of(existing));

        educationService.updateEducation(10L, request("New School"));

        assertEquals("New School", existing.getInstitutionName());
    }

    @Test
    void updateEducation_whenSomeoneElsesRecord_throwsAccessDeniedAndChangesNothing() {
        when(securityUtils.getCurrentUser()).thenReturn(userWithProfile(profileWithId(1L)));
        Education theirs = educationOwnedBy(profileWithId(2L), "Their School");
        when(educationRepository.findById(10L)).thenReturn(Optional.of(theirs));

        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> educationService.updateEducation(10L, request("Hacked")));

        assertEquals("Their School", theirs.getInstitutionName());
        assertTrue(ex.getMessage().contains("update"));   // message names the action
    }

    @Test
    void updateEducation_whenIdMissing_throwsNotFound() {
        when(educationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> educationService.updateEducation(99L, request("x")));

        verifyNoInteractions(securityUtils);
    }



    @Test
    void deleteEducation_whenOwner_deletesIt() {
        CandidateProfile myProfile = profileWithId(1L);
        when(securityUtils.getCurrentUser()).thenReturn(userWithProfile(myProfile));
        Education mine = educationOwnedBy(myProfile, "Mine");
        when(educationRepository.findById(10L)).thenReturn(Optional.of(mine));

        educationService.deleteEducation(10L);

        verify(educationRepository).delete(mine);
    }

    @Test
    void deleteEducation_whenSomeoneElsesRecord_throwsAccessDeniedAndDeletesNothing() {
        when(securityUtils.getCurrentUser()).thenReturn(userWithProfile(profileWithId(1L)));
        Education theirs = educationOwnedBy(profileWithId(2L), "Theirs");
        when(educationRepository.findById(10L)).thenReturn(Optional.of(theirs));

        assertThrows(AccessDeniedException.class, () -> educationService.deleteEducation(10L));

        verify(educationRepository, never()).delete(any(Education.class));
    }

    @Test
    void deleteEducation_whenIdMissing_throwsNotFound() {
        when(educationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> educationService.deleteEducation(99L));

        verify(educationRepository, never()).delete(any(Education.class));
    }

    // ---------- the bug: user has no profile ----------

    @Test
    void deleteEducation_whenCurrentUserHasNoProfile_throwsAccessDenied_notNpe() {
        when(securityUtils.getCurrentUser()).thenReturn(new User());   // candidateProfile is null
        Education theirs = educationOwnedBy(profileWithId(2L), "Theirs");
        when(educationRepository.findById(10L)).thenReturn(Optional.of(theirs));

        assertThrows(AccessDeniedException.class, () -> educationService.deleteEducation(10L));
        verify(educationRepository, never()).delete(any(Education.class));
    }

    @Test
    void updateEducation_whenCurrentUserHasNoProfile_throwsAccessDenied_notNpe() {
        when(securityUtils.getCurrentUser()).thenReturn(new User());
        Education theirs = educationOwnedBy(profileWithId(2L), "Theirs");
        when(educationRepository.findById(10L)).thenReturn(Optional.of(theirs));

        assertThrows(AccessDeniedException.class,
                () -> educationService.updateEducation(10L, request("x")));
        assertEquals("Theirs", theirs.getInstitutionName());
    }

    // ---------- list ----------

    @Test
    void getAllEducationsById_returnsNewestFirst() {
        CandidateProfile profile = profileWithId(1L);
        Education older = educationOwnedBy(profile, "Older");
        older.setStartDate(LocalDate.of(2015, 1, 1));
        Education newer = educationOwnedBy(profile, "Newer");
        newer.setStartDate(LocalDate.of(2019, 1, 1));
        when(educationRepository.findByCandidateProfileId(1L)).thenReturn(List.of(older, newer));

        List<EducationResponse> result = educationService.getAllEducationsById(1L);

        assertEquals("Newer", result.get(0).institutionName());
        assertEquals("Older", result.get(1).institutionName());
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

    private Education educationOwnedBy(CandidateProfile profile, String institution) {
        Education e = new Education();
        e.setInstitutionName(institution);
        e.setStartDate(LocalDate.of(2016, 1, 1));
        e.setCandidateProfile(profile);
        return e;
    }

    private EducationRequest request(String institution) {
        return new EducationRequest(institution,"Bachelors","Compute science",LocalDate.of(2023,10,10),
                null,true,null);
    }
}
