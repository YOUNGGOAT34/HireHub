package com.goat.HireHub.job;

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


    public static Specification<Job> salaryAtMost(BigDecimal maxSalary){
        return (root,query,criteriaBuilder)->{
            if (maxSalary==null) return null;

            return criteriaBuilder.lessThanOrEqualTo(root.get("salary"),maxSalary);
        };
    }

     public static Specification<Job> byCompany(String companyName){
          return (root,query,criteriaBuilder)->{
               if(companyName==null || companyName.isBlank()) return null;
               return criteriaBuilder.equal(criteriaBuilder.lower(root.get("company").get("name")),companyName.trim().toLowerCase());
          };
     }

    public static Specification<Job> byCompanyId(Long companyId) {
         return (root, query, criteriaBuilder) ->{
               if(companyId==null) return null;
               return criteriaBuilder.equal(root.get("company").get("id"),companyId);
         };

    }

    public static Specification<Job> byLocation(String location) {
        return (root,query,criteriaBuilder)->{
            if(location==null || location.isBlank()) return null;
            return criteriaBuilder.equal(criteriaBuilder.lower(root.get("location")),location.trim().toLowerCase());
        };
    }

    public static Specification<Job> byWorkPlaceType(WorkPlaceType workPlaceType) {
        return (root,query,criteriaBuilder)->{
            if(workPlaceType==null) return null;
            return criteriaBuilder.equal(root.get("workPlaceType"),workPlaceType);
        };
    }

    public static Specification<Job> byExperienceLevel(ExperienceLevel experienceLevel) {
        return (root,query,criteriaBuilder)->{
            if(experienceLevel==null) return null;
            return criteriaBuilder.equal(root.get("experienceLevel"),experienceLevel);
        };
    }

    public static Specification<Job> byEmplymentType(EmploymentType employmentType) {
        return (root,query,criteriaBuilder)->{
            if(employmentType==null) return null;
            return criteriaBuilder.equal(root.get("employmentType"),employmentType);
        };
    }

    public static Specification<Job> withFilters(
            String title,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            String location,
            String companyName,
            Long companyId,
            WorkPlaceType workPlaceType,
            ExperienceLevel experienceLevel,
            EmploymentType employmentType
    ){
        return JobSpecification
                .titleContains(title)
                .and(JobSpecification.salaryAtLeast(minSalary))
                .and(JobSpecification.byCompany(companyName))
                .and(JobSpecification.salaryAtMost(maxSalary))
                .and(JobSpecification.byLocation(location))
                .and(JobSpecification.byCompanyId(companyId))
                .and(JobSpecification.byWorkPlaceType(workPlaceType))
                .and(JobSpecification.byExperienceLevel(experienceLevel))
                .and(JobSpecification.byEmplymentType(employmentType));
    }
}
