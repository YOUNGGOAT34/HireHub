package profile;

import com.goat.HireHub.user.dto.FullProfileResponse;
import com.goat.HireHub.user.dto.ProfileSummaryResponse;
import com.goat.HireHub.user.dto.ProfileSummaryUpdateRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Validated
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    public CandidateProfileController(CandidateProfileService candidateProfileService){
         this.candidateProfileService=candidateProfileService;
    }
    @GetMapping("/{id}/profile")
    public ResponseEntity<FullProfileResponse> getPublicProfile(
            @PathVariable @Positive(message = "Id must be greater than zero") Long id) {
        return ResponseEntity.ok(candidateProfileService.getFullProfileByUserId(id));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ProfileSummaryResponse> updateMyProfile(
            @RequestBody @Valid ProfileSummaryUpdateRequest request) {
        ProfileSummaryResponse response = candidateProfileService.updateMainProfile(request);
        return ResponseEntity.ok(response);
    }
}
