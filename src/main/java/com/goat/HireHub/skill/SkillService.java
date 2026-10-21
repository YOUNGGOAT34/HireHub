package com.goat.HireHub.skill;

import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.skill.dto.SkillRequest;
import com.goat.HireHub.skill.dto.SkillResponse;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import jakarta.transaction.Transactional;
import profile.CandidateProfile;
import profile.CandidateProfileService;

import java.util.List;

public class SkillService {

    private final SkillRepository skillRepository;
    private final SecurityUtils securityUtils;
    private final CandidateProfileService candidateProfileService;
    private final UserRepository userRepository;


    public SkillService(SkillRepository skillRepository, SecurityUtils securityUtils,CandidateProfileService candidateProfileService,
                        UserRepository userRepository){
         this.skillRepository=skillRepository;
         this.securityUtils=securityUtils;
         this.candidateProfileService=candidateProfileService;
         this.userRepository=userRepository;
    }

    //skills
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
        return SkillResponse.from(skillRepository.save(skill));
    }

    public void removeSkillFromProfile(Long skillId){
        User loggedInUser = getCurrentUser();
        CandidateProfile profile = loggedInUser.getCandidateProfile();

        if (profile == null) {
            throw new ResourceNotFoundException("Profile not found for current session user.");
        }

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill record not found in system taxonomy registry", skillId));

        profile.getSkills().remove(skill);
    }

    private User getCurrentUser(){
        return securityUtils.getCurrentUser();
    }
}
