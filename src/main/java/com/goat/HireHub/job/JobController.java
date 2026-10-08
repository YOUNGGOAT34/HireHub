package com.goat.HireHub.job;


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

      public ResponseEntity<List<Job>> getAll(){

        return ResponseEntity.ok(jobService.getAll());
      }

      //get a job by an id
      @GetMapping("/{id}")
      public ResponseEntity<Job> getById(@PathVariable Long id){
            return ResponseEntity.ok(jobService.getById(id));
      }

      @PostMapping
     public ResponseEntity<Job> addJob(@RequestBody Job job){
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.addJob(job));
      }

      @DeleteMapping("/{id}")
      public ResponseEntity<Void> deleteJob(@PathVariable Long id){
          jobService.deleteById(id);
          return ResponseEntity.noContent().build();
      }

      @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id,@RequestBody Job updatedJob){
        return ResponseEntity.status(HttpStatus.OK).body(jobService.updateJob(id, updatedJob));
      }

      @PatchMapping("/{id}")
      public ResponseEntity<Job> patchJob(@PathVariable Long id,@RequestBody Job updatedJob) {
          return ResponseEntity.ok(jobService.patchJob(id, updatedJob));
      }
}
