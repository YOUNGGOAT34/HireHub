package com.goat.HireHub.job;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/jobs")
public class Controller {
     private List<Job> jobs=new ArrayList<>();
     @GetMapping("/")
     /*
        Get all  jobs
      */
      public List<Job> getAll(){

        return jobs;
      }

      @PostMapping("/")
     public String addJob(@RequestBody Job job){
          jobs.add(job);
          return "Job created successfully";
      }
}
