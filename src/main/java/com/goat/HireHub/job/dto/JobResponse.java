package com.goat.HireHub.job.dto;

import com.goat.HireHub.job.Job;

import java.math.BigDecimal;

public record JobResponse(Long id, String title, String description, BigDecimal salary,Long companyId,String companyName) {
     public static JobResponse from(Job job){
           return new JobResponse(job.getId(), job.getTitle(),job.getDescription(),job.getSalary(),job.getCompany().getId(),job.getCompany().getName());
     }
}
