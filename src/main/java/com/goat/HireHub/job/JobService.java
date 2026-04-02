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

    public Job addJob(Job job){
        jobs.add(job);
        return job;
    }

    public Job getById(Long id){
         for(Job job:jobs){
              if (job.getId().equals(id)){
                  return job;
             }
         }
         return null;
    }

    public boolean deleteJob(Long id){
         for(Job job:jobs){
             if(job.getId().equals(id)){
                 jobs.remove(job);
                 return true;
             }
         }

         return false;
    }

    public Job updateJob(Long id,Job updatedJob){

        for(Job job:jobs){
            if(job.getId().equals(id)){
                job.setSalary(updatedJob.getSalary());
                job.setTitle(updatedJob.getTitle());
                job.setDescription(updatedJob.getDescription());
                return  job;
            }
        }

        return null;
    }


    public Job patchJob(Long id,Job updatedJob){
        for(Job job:jobs){
            if(updatedJob.getSalary()!=null){
                 job.setSalary((updatedJob.getSalary()));
            }

            if(updatedJob.getTitle()!=null){
                job.setTitle(updatedJob.getTitle());
            }

            if(updatedJob.getDescription()!=null){
                job.setDescription(updatedJob.getDescription());
            }

            return job;
        }
        return null;
    }

}

