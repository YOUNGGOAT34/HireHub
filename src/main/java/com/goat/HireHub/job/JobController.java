package com.goat.HireHub.job;


import com.goat.HireHub.job.dto.JobPatchRequest;
import com.goat.HireHub.job.dto.JobRequest;
import com.goat.HireHub.job.dto.JobResponse;
import com.goat.HireHub.job.dto.JobUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService service) {
        this.jobService = service;
    }

    /*
      Get all  jobs
    */
    @GetMapping

      public ResponseEntity<List<JobResponse>> getAll(){

        return ResponseEntity.ok(jobService.getAll());
      }

      //get a job by an id
      @GetMapping("/{id}")
      public ResponseEntity<JobResponse> getById(@PathVariable Long id){
            return ResponseEntity.ok(jobService.getById(id));
      }

      @PostMapping
     public ResponseEntity<JobResponse> addJob(@Valid @RequestBody JobRequest job){
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.addJob(job));
      }

      @DeleteMapping("/{id}")
      public ResponseEntity<Void> deleteJob(@PathVariable Long id){
          jobService.deleteById(id);
          return ResponseEntity.noContent().build();
      }

      @PutMapping("/{id}")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id,@Valid @RequestBody JobUpdateRequest updatedJob){
        return ResponseEntity.status(HttpStatus.OK).body(jobService.updateJob(id, updatedJob));
      }

      @PatchMapping("/{id}")
      public ResponseEntity<JobResponse> patchJob(@PathVariable Long id,@Valid @RequestBody JobPatchRequest updatedJob) {
          return ResponseEntity.ok(jobService.patchJob(id, updatedJob));
      }
}
