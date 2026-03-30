package com.goat.HireHub.job;

public class Job {
       private Long Id;
       private String Title;
       private String Description;
       private  Double Salary;

    public Job(Double salary, String description, String title, Long id) {
        Salary = salary;
        Description = description;
        Title = title;
        Id = id;
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public Double getSalary() {
        return Salary;
    }

    public void setSalary(Double salary) {
        Salary = salary;
    }
}
