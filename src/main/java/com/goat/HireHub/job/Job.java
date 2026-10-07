package com.goat.HireHub.job;

import com.goat.HireHub.company.Company;
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
       @ManyToOne
       @JoinColumn(name="company_id")
       private Company company;

       protected Job(){}

    public Job(Double salary, String description, String title,Company company) {
        this.salary = salary;
        this.description = description;
        this.title = title;
        this.company=company;
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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
