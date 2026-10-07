package com.goat.HireHub.company;

import com.goat.HireHub.job.Job;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name="companies")
public class Company {

     @Id
     @GeneratedValue(strategy = GenerationType.SEQUENCE)
     @Column(name="company_id")
     private Long Id;
     @Column(name="company_name")
     private String name;
     @Column(name="company_description")
     private String description;
     @OneToMany(mappedBy = "company")
     private List<Job> jobs;

     protected Company(){}

     public Company(String name,String description){
           this.description=description;
           this.name=name;
     }

     public Long getId() {
          return Id;
     }

     public void setId(Long id) {
          Id = id;
     }

     public String getName() {
          return name;
     }

     public void setName(String name) {
          this.name = name;
     }

     public String getDescription() {
          return description;
     }

     public void setDescription(String description) {
          this.description = description;
     }

     public List<Job> getJobs() {
          return jobs;
     }

     public void setJobs(List<Job> jobs) {
          this.jobs = jobs;
     }
}
