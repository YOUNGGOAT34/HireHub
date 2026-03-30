package com.goat.HireHub.job;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobService {
    private List<Job> jobs=new ArrayList<>();


    public List<Job> getAll(){
        return jobs;
    }

    public String addJob(Job job){
        jobs.add(job);
        return "Job created successfully";
    }

    public Job getById(Long id){
         for(Job job:jobs){
              if (job.getId()==id){
                  return job;
             }
         }
         return null;
    }


}
