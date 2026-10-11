package com.goat.HireHub.job;

import org.springframework.beans.factory.BeanRegistry;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class JobSpecification {

     public static Specification<Job> titleContains(String title){
          return (root,query,criteriaBuilder)->{
                if(title==null || title.isBlank()) return null;
                return criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),"%"+ title.toLowerCase()+"%");
          };
     }

     public static Specification<Job> salaryAtLeast(BigDecimal minSalary){
           return (root,query,criteriaBuilder)->{
                 if (minSalary==null) return null;

                 return criteriaBuilder.greaterThanOrEqualTo(root.get("salary"),minSalary);
           };
     }
}
