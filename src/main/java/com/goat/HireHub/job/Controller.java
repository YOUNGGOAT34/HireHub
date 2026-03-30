package com.goat.HireHub.job;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/jobs")
public class Controller {

    private JobService service;

    public Controller(JobService service) {
        this.service = service;
    }

    @GetMapping("/")
     /*
        Get all  jobs
      */
      public List<Job> getAll(){
        return service.getAll();
      }

      @GetMapping("/{id}")
      public Job getById(@PathVariable Long id){
           return service.getById(id);
      }

      @PostMapping("/")
     public String addJob(@RequestBody Job job){
          return service.addJob(job);
      }
}
