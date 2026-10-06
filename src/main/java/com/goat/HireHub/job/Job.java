package com.goat.HireHub.job;

import jakarta.persistence.*;

@Entity
@Table(name="jobs")
public class Job {
       @Id
       @GeneratedValue(strategy = GenerationType.SEQUENCE)
       @Column(name="job_id")
       private Long id;
       @Column(name="job_title")
       private String title;
       @Column(name="job_description")
       private String description;
       private  Double salary;

       protected Job(){}

    public Job(Double salary, String description, String title) {
        this.salary = salary;
        this.description = description;
        this.title = title;

    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public Double getSalary() {
        return this.salary;
    }
    public void setSalary(Double salary) {
        this.salary = salary;
    }
}
