package profile;

import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import com.goat.HireHub.user.dto.*;
import jakarta.transaction.Transactional;

import java.util.List;

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

}
