package com.goat.HireHub.experience;

import com.goat.HireHub.experience.dto.ExperienceRequest;
import com.goat.HireHub.experience.dto.ExperienceResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/users")
@Validated
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService){
         this.experienceService=experienceService;
    }

    @PostMapping("/me/experiences")
    public ResponseEntity<ExperienceResponse> addExperience(
            @RequestBody @Valid ExperienceRequest experienceRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(experienceService.addExperience(experienceRequest));
    }

    @GetMapping("/{id}/experiences")
    public ResponseEntity<List<ExperienceResponse>> getUserExperiences(
            @PathVariable @Positive(message = "Id must be greater than zero") Long id) {
        List<ExperienceResponse> responses = experienceService.getAllExperiencesByUserId(id);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/me/experiences/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable @Positive(message = "Id must be greater than zero") Long id){
        experienceService.deleteExperienceById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/experiences/{id}")
    public ResponseEntity<ExperienceResponse> updateExperience(@PathVariable @Positive(message = "id must be greater than zero")Long id,@RequestBody @Valid ExperienceRequest request){
        return ResponseEntity.ok(experienceService.updateExperience(id,request));
    }
}
