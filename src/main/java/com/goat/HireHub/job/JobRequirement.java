package com.goat.HireHub.job;

import jakarta.persistence.*;

@Entity
@Table(name="job_requirements")
public class JobRequirement{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name="requirement_id")
     private Long Id;
    @Column(name="job_id",nullable = false)
    @ManyToOne
    @JoinColumn(name="job_id")
    private Job job;
    @Column(name="display_order",nullable = false)
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
        return this.Id;
    }

    public void setId(Long id){
        this.Id=id;
    }

    public String getContent(){
        return this.content;
    }

    public void setContent(String content){
         this.content=content;
    }


}
