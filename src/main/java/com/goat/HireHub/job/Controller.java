package com.goat.HireHub.job;

import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/jobs")
public class Controller {

    private final JobService service;

    public Controller(JobService service) {
        this.service = service;
    }

    /*
      Get all  jobs
    */
    @GetMapping

      public ResponseEntity<List<Job>> getAll(){
        return ResponseEntity.ok(service.getAll());
      }

      //get a job by an id
      @GetMapping("/{id}")
      public ResponseEntity<Job> getById(@PathVariable Long id){
        Job job=service.getById(id);
        if(job!=null){
            return ResponseEntity.ok(job);
        }

        return ResponseEntity.notFound().build();

      }

      @PostMapping
     public ResponseEntity<Job> addJob(@RequestBody Job job){

        return ResponseEntity.status(HttpStatus.CREATED).body(service.addJob(job));
      }

      @DeleteMapping("/{id}")
      public ResponseEntity<Void> deleteJob(@PathVariable Long id){
          boolean deleted= service.deleteJob(id);
          if(deleted){
              return ResponseEntity.noContent().build();
          }
          return ResponseEntity.notFound().build();
      }

      @PutMapping("/{id}")
    public ResponseEntity<Job> updateJob(@PathVariable Long id,@RequestBody Job updatedJob){
             Job updated=service.updateJob(id, updatedJob);
             if(updated !=null){
                 return ResponseEntity.status(HttpStatus.OK).body(updated);
             }
             return ResponseEntity.notFound().build();
      }

      @PatchMapping("/{id}")
      public ResponseEntity<Job> patchJob(@PathVariable Long id,@RequestBody Job updatedJob){
         Job patched= service.patchJob(id, updatedJob);
         if(patched!=null){
             return ResponseEntity.ok(patched);
         }
         return ResponseEntity.notFound().build();
      }
}
