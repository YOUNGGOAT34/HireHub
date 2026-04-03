package com.goat.HireHub.job;

public class Job {
       private Long id;
       private String title;
       private String description;
       private  Double salary;

    public Job(Double salary, String description, String title, Long id) {
        this.salary = salary;
        this.description = description;
        this.title = title;
        this.id = id;
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
