package com.goat.HireHub.profile;

import com.goat.HireHub.education.dto.EducationResponse;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.experience.dto.ExperienceResponse;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.SearchStatus;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import jakarta.transaction.Transactional;
import com.goat.HireHub.profile.dto.FullProfileResponse;
import com.goat.HireHub.profile.dto.ProfileSummaryResponse;
import com.goat.HireHub.profile.dto.ProfileSummaryUpdateRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CandidateProfileService {
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final CandidateProfileRepository candidateProfileRepository;

    public CandidateProfileService(UserRepository userRepository,SecurityUtils securityUtils,CandidateProfileRepository candidateProfileRepository){
        this.userRepository=userRepository;
        this.securityUtils=securityUtils;
        this.candidateProfileRepository=candidateProfileRepository;
    }
    //profile
    @Transactional
    public FullProfileResponse getFullProfileByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        CandidateProfile profile = user.getCandidateProfile();
        List<ExperienceResponse> expList = (profile != null)
                ? profile.getExperiences().stream().map(ExperienceResponse::from).toList() : List.of();
        List<EducationResponse> eduList = (profile != null)
                ? profile.getEducations().stream().map(EducationResponse::from).toList() : List.of();

        return new FullProfileResponse(
                user.getId(), user.getFirstName(), user.getLastName(),user.getUsername(),
                (profile != null) ? profile.getHeadline() : null,
                (profile != null) ? profile.getSummary() : null,
                (profile != null) ? profile.getSearchStatus() : null,
                expList, eduList
        );
    }

    @Transactional
    public ProfileSummaryResponse updateMainProfile(ProfileSummaryUpdateRequest request) {
        User loggedInUser = securityUtils.getCurrentUser();
        CandidateProfile profile = getOrCreateCandidateProfile(loggedInUser);
        profile.setSummary(request.summary());
        profile.setHeadline(request.headline());
        profile.setSearchStatus(request.searchStatus());
        return new ProfileSummaryResponse(profile.getId(), profile.getHeadline(), profile.getSummary(), profile.getSearchStatus());
    }


        @Transactional
        public CandidateProfile getOrCreateCandidateProfile(User user) {
            return candidateProfileRepository.findById(user.getId())
                    .orElseGet(() -> {
                        CandidateProfile p = new CandidateProfile();
                        p.setUser(user);
                        p.setSearchStatus(SearchStatus.OPEN_TO_WORK);
                        p.setHeadline("Software Professional");
                        return candidateProfileRepository.save(p);
                    });
        }


}
