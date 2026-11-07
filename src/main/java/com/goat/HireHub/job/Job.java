package com.goat.HireHub.job;

import com.goat.HireHub.company.Company;
import com.goat.HireHub.skill.Skill;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="jobs")
public class Job {
       @Id
       @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "jobs_seq")
       @SequenceGenerator(
               name = "experiences_seq_gen",
               sequenceName = "experiences_seq",
               allocationSize = 50
       )
       @Column(name="job_id")
       private Long id;
       @Column(name="job_title")
       private String title;
       @Column(name="job_description",length=500)
       private String description;
       private BigDecimal salary;
       @ManyToOne(fetch = FetchType.LAZY)
       @JoinColumn(name="company_id")
       private Company company;
       @OneToMany(mappedBy = "job",cascade =CascadeType.ALL,orphanRemoval = true)
       @OrderBy("displayOrder ASC")
       private List<JobRequirement> requirements=new ArrayList<>();
       @ManyToMany
       @JoinTable(
           name="job_skills",
           joinColumns = @JoinColumn(name="job_id"),
           inverseJoinColumns = @JoinColumn(name="skill_id")
       )
       private Set<Skill> jobSkills=new HashSet<>();

       public Job(){}

    public Job(BigDecimal salary, String description, String title,Company company) {
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
    public BigDecimal getSalary() {
        return this.salary;
    }
    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }



}
