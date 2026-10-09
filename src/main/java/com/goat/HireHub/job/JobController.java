package com.goat.HireHub.job;


import com.goat.HireHub.job.dto.JobPatchRequest;
import com.goat.HireHub.job.dto.JobRequest;
import com.goat.HireHub.job.dto.JobResponse;
import com.goat.HireHub.job.dto.JobUpdateRequest;
import jakarta.validation.Valid;
import org.hibernate.query.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;


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

      public ResponseEntity<Page<JobResponse>> getAll(@RequestParam(defaultValue = "0")int page,@RequestParam(defaultValue = "10")int size){

        Pageable pageable= PageRequest.of(page,size);

        return ResponseEntity.ok(jobService.getAll(pageable));
      }

      //get a job by an id
      @GetMapping("/{id}")
      public ResponseEntity<JobResponse> getById(@PathVariable Long id){
            return ResponseEntity.ok(jobService.getById(id));
      }
      //search
    @GetMapping("/search")
    public Page<JobResponse> searchByTitle(@RequestParam String title,
                                           @RequestParam(defaultValue = "0")int page,
                                           @RequestParam(defaultValue = "10")int size,
                                           @RequestParam(defaultValue = "salary")String sortBy,
                                           @RequestParam(defaultValue = "desc")String direction){
        Set<String> allowedSortFields = Set.of(
                "title",
                "salary"
        );

        if (!allowedSortFields.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sortBy
            );
        }
        Sort.Direction sortDirection=Sort.Direction.fromString(direction);
          Pageable pageable=PageRequest.of(page,size, Sort.by(sortDirection,sortBy));
          return jobService.searchByTitle(title,pageable);
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
