package com.goat.HireHub.skill;

import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.profile.CandidateProfile;
import com.goat.HireHub.profile.CandidateProfileRepository;
import com.goat.HireHub.profile.CandidateProfileService;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.skill.dto.SkillRequest;
import com.goat.HireHub.skill.dto.SkillResponse;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    SkillRepository skillRepository;
    @Mock
    SecurityUtils securityUtils;
    @Mock
    CandidateProfileService candidateProfileService;
    @Mock
    UserRepository userRepository;
    @Mock
    CandidateProfileRepository candidateProfileRepository;

    @InjectMocks
    SkillService skillService;


    @Test
    void addSkill_whenSkillAlreadyExists_reusesItCaseInsensitivelyAndDoesNotSaveAgain() {
        User me = userWithId(1L);
        CandidateProfile profile = new CandidateProfile();
        Skill existing = new Skill("Java");
        when(securityUtils.getCurrentUser()).thenReturn(me);
        when(candidateProfileService.getOrCreateCandidateProfile(me)).thenReturn(profile);
        when(skillRepository.findByNameIgnoreCase("java")).thenReturn(Optional.of(existing));

        SkillResponse response = skillService.addSkillToProfile(new SkillRequest("  java "));

        assertTrue(profile.getSkills().contains(existing));
        assertEquals("Java", response.name());
        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    void addSkill_whenSkillIsNew_createsItWithTrimmedName() {
        User me = userWithId(1L);
        CandidateProfile profile = new CandidateProfile();
        when(securityUtils.getCurrentUser()).thenReturn(me);
        when(candidateProfileService.getOrCreateCandidateProfile(me)).thenReturn(profile);
        when(skillRepository.findByNameIgnoreCase("Rust")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenAnswer(inv -> inv.getArgument(0));

        skillService.addSkillToProfile(new SkillRequest("  Rust  "));

        ArgumentCaptor<Skill> captor = ArgumentCaptor.forClass(Skill.class);
        verify(skillRepository).save(captor.capture());
        assertEquals("Rust", captor.getValue().getName());
        assertEquals(1, profile.getSkills().size());
    }

    @Test
    void addSkill_whenProfileAlreadyHasIt_throwsConflictAndAddsNothing() {
        User me = userWithId(1L);
        Skill java = new Skill("Java");
        CandidateProfile profile = new CandidateProfile();
        profile.getSkills().add(java);
        when(securityUtils.getCurrentUser()).thenReturn(me);
        when(candidateProfileService.getOrCreateCandidateProfile(me)).thenReturn(profile);
        when(skillRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.of(java));

        assertThrows(ConflictException.class,
                () -> skillService.addSkillToProfile(new SkillRequest("Java")));

        assertEquals(1, profile.getSkills().size());
    }


    @Test
    void removeSkill_whenOnProfile_removesIt() {
        User me = userWithId(1L);
        Skill java = new Skill("Java");
        CandidateProfile profile = new CandidateProfile();
        profile.getSkills().add(java);
        when(securityUtils.getCurrentUser()).thenReturn(me);
        when(candidateProfileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(skillRepository.findById(5L)).thenReturn(Optional.of(java));
        skillService.removeSkillFromProfile(5L);
        assertTrue(profile.getSkills().isEmpty());
    }

    @Test
    void removeSkill_whenSkillNotOnMyProfile_throwsNotFound() {
        User me = userWithId(1L);
        CandidateProfile profile = new CandidateProfile();
        when(securityUtils.getCurrentUser()).thenReturn(me);

        assertThrows(ResourceNotFoundException.class,
                () -> skillService.removeSkillFromProfile(5L));
    }

    @Test
    void removeSkill_whenSkillIdMissing_throwsNotFound() {
        User me = userWithId(1L);
        when(securityUtils.getCurrentUser()).thenReturn(me);
        when(candidateProfileRepository.findById(1L)).thenReturn(Optional.of(new CandidateProfile()));
        when(skillRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> skillService.removeSkillFromProfile(99L));
    }

    @Test
    void removeSkill_whenUserHasNoProfile_throwsNotFoundBeforeTouchingSkills() {
        User me = userWithId(1L);
        when(securityUtils.getCurrentUser()).thenReturn(me);
        when(candidateProfileRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> skillService.removeSkillFromProfile(5L));

        verifyNoInteractions(skillRepository);
    }


    @Test
    void getSkillsByProfileId_whenUserMissing_throwsNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> skillService.getSkillsByProfileId(99L));

        verifyNoInteractions(skillRepository);
    }

    @Test
    void getSkillsByProfileId_returnsNamesSortedIgnoringCase() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(skillRepository.findAllByProfileId(1L)).thenReturn(
                List.of(new Skill("banana"), new Skill("Apple"), new Skill("cherry")));

        List<SkillResponse> result = skillService.getSkillsByProfileId(1L);

        assertEquals(List.of("Apple", "banana", "cherry"),
                result.stream().map(SkillResponse::name).toList());
    }

    private User userWithId(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }
}
