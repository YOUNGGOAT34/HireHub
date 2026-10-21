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

    public CandidateProfileService(UserRepository userRepository,SecurityUtils securityUtils){
        this.userRepository=userRepository;
        this.securityUtils=securityUtils;
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
                user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getUsername(),
                (profile != null) ? profile.getHeadline() : null,
                (profile != null) ? profile.getSummary() : null,
                (profile != null) ? profile.getSearchStatus() : null,
                expList, eduList
        );
    }

    @Transactional
    public ProfileSummaryResponse updateMainProfile(ProfileSummaryUpdateRequest request) {
        User loggedInUser = securityUtils.getCurrentUser();
        CandidateProfile profile = loggedInUser.getCandidateProfile();

        if (profile == null) {
            profile = new CandidateProfile(loggedInUser, request.headline(), request.summary(), request.searchStatus());
            loggedInUser.setCandidateProfile(profile);
        } else {
            profile.setHeadline(request.headline());
            profile.setSummary(request.summary());
            profile.setSearchStatus(request.searchStatus());
        }

        userRepository.save(loggedInUser);
        return new ProfileSummaryResponse(profile.getId(), profile.getHeadline(), profile.getSummary(), profile.getSearchStatus());
    }

    public CandidateProfile getOrCreateCandidateProfile(User user) {
        CandidateProfile profile = user.getCandidateProfile();

        if (profile != null) {
            return profile;
        }

        profile = new CandidateProfile();
        profile.setUser(user);
        profile.setSearchStatus(SearchStatus.OPEN_TO_WORK);
        profile.setHeadline("Software Professional");

        user.setCandidateProfile(profile);

        userRepository.save(user);

        return profile;
    }

}
