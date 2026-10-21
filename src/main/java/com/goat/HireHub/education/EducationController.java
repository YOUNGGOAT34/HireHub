package com.goat.HireHub.education;

import com.goat.HireHub.education.dto.EducationRequest;
import com.goat.HireHub.education.dto.EducationResponse;
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
public class EducationController {

    private final EducationService educationService;

    public EducationController(EducationService educationService){
         this.educationService=educationService;
    }

    @PostMapping("/me/educations")
    public ResponseEntity<EducationResponse> addEducation(@RequestBody @Valid EducationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(educationService.addEducation(request));
    }

    @PutMapping("/me/educations/{id}")
    public ResponseEntity<EducationResponse> updateEducation(
            @PathVariable @Positive(message = "Id must be greater than zero") Long id,
            @RequestBody @Valid EducationRequest request) {
        return ResponseEntity.ok(educationService.updateEducation(id, request));
    }

    @DeleteMapping("/me/educations/{id}")
    public ResponseEntity<Void> deleteEducation(
            @PathVariable @Positive(message = "Id must be greater than zero") Long id) {
        educationService.deleteEducation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/educations")
    public ResponseEntity<List<EducationResponse>> getUserEducations(
            @PathVariable @Positive(message = "Id must be greater than zero") Long id) {

        return ResponseEntity.ok(educationService.getAllEducationsById(id));
    }
}
