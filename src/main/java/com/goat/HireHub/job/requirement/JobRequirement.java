package com.goat.HireHub.job.requirement;

import com.goat.HireHub.job.Job;
import jakarta.persistence.*;

@Entity
@Table(name="job_requirements")
public class JobRequirement{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "job_requirements_seq")
    @SequenceGenerator(
            name = "job_requirements_seq",
            sequenceName = "job_requirements_seq",
            allocationSize = 50
    )
    @Column(name="requirement_id")
     private Long id;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name="job_id",nullable = false)
    private Job job;
    @Column(name="display_order")
    private Integer displayOrder;
    @Enumerated(EnumType.STRING)
    @Column(name = "requirement_type",nullable = false)
    private RequirementType requirementType;
    @Column(name="content",nullable = false,columnDefinition = "TEXT")
    private String content;

    public JobRequirement(){}

    public JobRequirement(String content){
        this.content=content;
    }

    public void setJob(Job job){
        this.job=job;
    }

    public Job getJob(){
         return this.job;
    }

    public Integer getDisplayOrder(){
        return this.displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder){
        this.displayOrder=displayOrder;
    }

    public RequirementType getRequirementType(){
         return this.requirementType;
    }

    public void setRequirementType(RequirementType requirementType){
         this.requirementType=requirementType;
    }

    public Long getId(){
        return this.id;
    }

    public void setId(Long id){
        this.id=id;
    }

    public String getContent(){
        return this.content;
    }

    public void setContent(String content){
         this.content=content;
    }


}
