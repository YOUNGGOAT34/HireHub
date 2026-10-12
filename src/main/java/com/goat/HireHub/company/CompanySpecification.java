package com.goat.HireHub.company;

import org.springframework.data.jpa.domain.Specification;

public class CompanySpecification{
       public static Specification<Company> nameContains(String name){
            return(root,query,criteriaBuilder)->{
                 if(name==null || name.isBlank()) return null;

                 return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),"%"+name.toLowerCase()+"%");
            };
       }
}
