package com.goat.HireHub.experience;

import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import com.goat.HireHub.experience.dto.ExperienceRequest;
import com.goat.HireHub.experience.dto.ExperienceResponse;
import jakarta.transaction.Transactional;
import com.goat.HireHub.profile.CandidateProfile;
import com.goat.HireHub.profile.CandidateProfileService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final SecurityUtils securityUtils;
    private final CandidateProfileService candidateProfileService;


    public ExperienceService(ExperienceRepository experienceRepository,SecurityUtils securityUtils,CandidateProfileService candidateProfileService){
         this.experienceRepository=experienceRepository;
         this.securityUtils=securityUtils;
         this.candidateProfileService=candidateProfileService;
    }


    @Transactional
    public ExperienceResponse addExperience(ExperienceRequest request){
        User user=securityUtils.getCurrentUser();
        CandidateProfile profile=candidateProfileService.getOrCreateCandidateProfile(user);
        Experience experience=new Experience();
        experience.setJobTitle(request.jobTitle());
        experience.setCurrent(request.isCurrent());
        experience.setStartDate(request.startDate());
        experience.setEndDate(request.endDate());
        experience.setCompanyNameRaw(request.companyName());
        experience.setDescription(request.description());
        experience.setCandidateProfile(profile);
        return ExperienceResponse.from(experienceRepository.save(experience));
    }


    public List<ExperienceResponse> getAllExperiencesByUserId(Long userId){
        return experienceRepository.findByCandidateProfileId(userId).stream()
                .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
                .map(ExperienceResponse::from).toList();
    }

    @Transactional
    public void deleteExperienceById(Long id){
        Experience experience= authorizeExperienceAction(id,"delete");
        experienceRepository.delete(experience);
    }

    @Transactional
    public ExperienceResponse updateExperience(Long id,ExperienceRequest request){
        Experience savedExperience= authorizeExperienceAction(id,"update");
        savedExperience.setJobTitle(request.jobTitle());
        savedExperience.setCurrent(request.isCurrent());
        savedExperience.setStartDate(request.startDate());
        savedExperience.setEndDate(request.endDate());
        savedExperience.setCompanyNameRaw(request.companyName());
        savedExperience.setDescription(request.description());

        return ExperienceResponse.from(savedExperience);

    }

    private Experience authorizeExperienceAction(Long id, String action){
        Experience experience=findOrThrow(id);
        User loggedInUser=securityUtils.getCurrentUser();
        CandidateProfile profile=loggedInUser.getCandidateProfile();

        if (profile== null || !profile.getId().equals(experience.getCandidateProfile().getId())) {
            throw new AccessDeniedException(
                    "You do not have permission to " + action + " this experience record.");
        }

        return experience;
    }


    private Experience findOrThrow(Long experienceId){
        return experienceRepository.findById(experienceId)
                .orElseThrow(()->new ResourceNotFoundException("experience",experienceId));
    }
}
