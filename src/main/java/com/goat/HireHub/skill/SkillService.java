package com.goat.HireHub.skill;

import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.profile.CandidateProfileRepository;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.skill.dto.SkillRequest;
import com.goat.HireHub.skill.dto.SkillResponse;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import jakarta.transaction.Transactional;
import com.goat.HireHub.profile.CandidateProfile;
import com.goat.HireHub.profile.CandidateProfileService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final SecurityUtils securityUtils;
    private final CandidateProfileService candidateProfileService;
    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;


    public SkillService(SkillRepository skillRepository, SecurityUtils securityUtils,CandidateProfileService
                                candidateProfileService,
                        UserRepository userRepository,
                        CandidateProfileRepository candidateProfileRepository){
         this.skillRepository=skillRepository;
         this.securityUtils=securityUtils;
         this.candidateProfileService=candidateProfileService;
         this.userRepository=userRepository;
         this.candidateProfileRepository=candidateProfileRepository;
    }

    @Transactional
    public List<SkillResponse> getSkillsByProfileId(Long userId){
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        return skillRepository.findAllByProfileId(userId)
                .stream()
                .map(SkillResponse::from)
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .toList();
    }
    @Transactional
    public SkillResponse addSkillToProfile(SkillRequest skillRequest){

        User loggedInUser = getCurrentUser();
        CandidateProfile profile =candidateProfileService.getOrCreateCandidateProfile(loggedInUser);

        String cleanSkillName=skillRequest.name().trim();

        Skill skill = skillRepository.findByNameIgnoreCase(cleanSkillName)
                .orElseGet(() -> skillRepository.save(new Skill(cleanSkillName)));

        if (profile.getSkills().contains(skill)) {
            throw new ConflictException("You have already added the skill '" + cleanSkillName + "' to your profile.");
        }

        profile.getSkills().add(skill);

        return SkillResponse.from(skill);
    }

    @Transactional
    public void removeSkillFromProfile(Long skillId){
        User loggedInUser = getCurrentUser();
        CandidateProfile profile = candidateProfileRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile", loggedInUser.getId()));

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill record not found in system taxonomy registry", skillId));

        if (!profile.getSkills().remove(skill)){
            throw new ResourceNotFoundException("Skill", skillId);
        }
    }

    private User getCurrentUser(){
        return securityUtils.getCurrentUser();
    }
}
