package com.goat.HireHub.user;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "educations")
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "education_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CandidateProfile candidateProfile;

    @Column(name = "institution_name", nullable = false)
    private String institutionName;

    @Column(name = "degree_type")
    private String degreeType;

    @Column(name = "field_of_study")
    private String fieldOfStudy;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current")
    private boolean isCurrent;

    @Column(columnDefinition = "TEXT")
    private String activitiesAndSocieties;

    public Education() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CandidateProfile getCandidateProfile() { return candidateProfile; }
    public void setCandidateProfile(CandidateProfile candidateProfile) { this.candidateProfile = candidateProfile; }
    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
    public String getDegreeType() { return degreeType; }
    public void setDegreeType(String degreeType) { this.degreeType = degreeType; }
    public String getFieldOfStudy() { return fieldOfStudy; }
    public void setFieldOfStudy(String fieldOfStudy) { this.fieldOfStudy = fieldOfStudy; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }
    public String getActivitiesAndSocieties() { return activitiesAndSocieties; }
    public void setActivitiesAndSocieties(String activitiesAndSocieties) { this.activitiesAndSocieties = activitiesAndSocieties; }
}
