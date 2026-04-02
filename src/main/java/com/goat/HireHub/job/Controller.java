package com.goat.HireHub.job;

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
    @GetMapping("/")

      public List<Job> getAll(){
        return service.getAll();
      }

      //get a job by an id
      @GetMapping("/{id}")
      public Job getById(@PathVariable Long id){
           return service.getById(id);
      }

      @PostMapping("/")
     public String addJob(@RequestBody Job job){
          return service.addJob(job);
      }

      @DeleteMapping("/{id}")
      public String deleteJob(@PathVariable Long id){
          boolean deleted= service.deleteJob(id);
          if(deleted){
              return "Job deleted successfully";
          }
          return "Job not found";
      }

      @PostMapping("/{id}")
    public String updateJob(@PathVariable Long id,@RequestBody Job updatedJob){
             boolean updated=service.updateJob(id, updatedJob);
             if(updated){
                 return "Updated successfully";
             }

             return "Job not found";
      }

      @PatchMapping("/{id}")
      public String pathJob(@PathVariable Long id,@RequestBody Job updatedJob){
         boolean patched= service.patchJob(id, updatedJob);
         if(patched){
             return "Job updated successfully";
         }
         return "Job not found";
      }
}
