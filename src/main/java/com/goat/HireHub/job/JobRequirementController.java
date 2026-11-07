package com.goat.HireHub.job;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Validated
@RestController
@RequestMapping("/jobs/{jobId}/requirements")
public class JobRequirementController {
    private final JobRequirementService requirementService;

    public JobRequirementController(JobRequirementService requirementService){
         this.requirementService=requirementService;
    }

    @GetMapping
    public ResponseEntity<List<JobRequirementResponse>> getJobRequirements(
               @PathVariable @Positive(message = "job id must be greater than zero")
               Long jobId
    ){
         return ResponseEntity.ok(requirementService.getJobRequirements(jobId));
    }

    @PostMapping
    public ResponseEntity<JobRequirementResponse> addJobRequirement(
             @PathVariable @Positive(message="job id must be greater than zero")
             Long jobId,
             @RequestBody @Valid JobRequirementRequest request
    ){
        return ResponseEntity.status(HttpStatus.CREATED).body(requirementService.addJobRequirement(jobId,request));
    }

    @PutMapping("/{requirementId}")
    public ResponseEntity<JobRequirementResponse> updateJobRequirement(
             @PathVariable @Positive(message = "job id must be greater than zero")
             Long jobId,
             @PathVariable @Positive(message = "requirement id must be greater than zero")
             Long requirementId,
             @RequestBody @Valid JobRequirementRequest request
    ){
         return ResponseEntity.ok(requirementService.updateJobRequirements(request,jobId,requirementId));
    }

    @PatchMapping("/{requirementId}")
    public ResponseEntity<JobRequirementResponse> patchJobRequirement(
            @PathVariable @Positive(message = "job id must be greater than zero")
            Long jobId,
            @PathVariable @Positive(message = "requirement id must be greater than zero")
            Long requirementId,
            @RequestBody @Valid JobRequirementPatchRequest request
    ){
        return ResponseEntity.ok(requirementService.patchJobRequirement(jobId,requirementId,request));
    }

    @DeleteMapping("/{requirementId}")
    public ResponseEntity<Void> deleteJobRequirement(
            @PathVariable @Positive(message = "job id must be greater than zero")
            Long jobId,
            @PathVariable @Positive(message = "requirement id must be greater than zero")
            Long requirementId
    ){
        requirementService.deleteRequirement(jobId,requirementId);
        return ResponseEntity.noContent().build();
    }

}
