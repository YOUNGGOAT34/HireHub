package com.goat.HireHub.job;

import com.goat.HireHub.company.Company;
import com.goat.HireHub.job.requirement.JobRequirement;
import com.goat.HireHub.skill.Skill;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
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
               name = "jobs_seq",
               sequenceName = "jobs_seq",
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
       @Column(name="location",nullable = false)
       private String location;
       @Column(name="job_url",nullable = false,unique = true,columnDefinition = "TEXT")
       private String url;
       @Column(name="created_at",nullable = false,updatable = false)
       private Instant createdAt;
       @Column(name="workplace_type",nullable = false)
       @Enumerated(EnumType.STRING)
       private WorkPlaceType workPlaceType;
      @Column(name="experience_level",nullable = false)
      @Enumerated(EnumType.STRING)
       private ExperienceLevel experienceLevel;
        @Column(name="employment_type",nullable = false)
        @Enumerated(EnumType.STRING)
        private EmploymentType employmentType;
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

    public Job(BigDecimal salary, String description, String title,Company company,String url) {
        this.salary = salary;
        this.description = description;
        this.title = title;
        this.company=company;
        this.url=url;
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


    public void setUrl(String url){
           this.url=url;
    }

    public String getUrl(){
           return this.url;
    }

    public Instant getCreatedAt(){
           return this.createdAt;
    }

    public void setLocation(String location){
           this.location=location;
    }

    public String getLocation(){
           return this.location;
    }

    public void setWorkPlaceType(WorkPlaceType workPlaceType){
           this.workPlaceType=workPlaceType;
    }

    public WorkPlaceType getWorkPlaceType(){
           return this.workPlaceType;
    }

    public void setExperienceLevel(ExperienceLevel experienceLevel){
           this.experienceLevel=experienceLevel;
    }

    public ExperienceLevel getExperienceLevel(){
            return this.experienceLevel;
    }

    public void setEmploymentType(EmploymentType employmentType){
        this.employmentType=employmentType;
    }

    public EmploymentType getEmploymentType(){
        return this.employmentType;
    }

}
