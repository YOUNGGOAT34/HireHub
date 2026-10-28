package com.goat.HireHub.experience;

import com.goat.HireHub.company.Company;
import jakarta.persistence.*;
import com.goat.HireHub.profile.CandidateProfile;

import java.time.LocalDate;

@Entity
@Table(name = "experiences")
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name="experience_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CandidateProfile candidateProfile;

    @Column(name = "job_title", nullable = false)
    private String jobTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "company_name_raw")
    private String companyNameRaw; // Fallback text string if the company isn't registered on HireHub yet

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current",nullable = false)
    private boolean isCurrent;

    @Column(columnDefinition = "TEXT",nullable = false)
    private String description;

    public Experience() {}

    public Experience(CandidateProfile candidateProfile, String jobTitle, Company company,
                      String companyNameRaw, LocalDate startDate, LocalDate endDate,
                      boolean isCurrent, String description) {
        this.candidateProfile = candidateProfile;
        this.jobTitle = jobTitle;
        this.company = company;
        this.companyNameRaw = companyNameRaw;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CandidateProfile getCandidateProfile() { return candidateProfile; }
    public void setCandidateProfile(CandidateProfile candidateProfile) { this.candidateProfile = candidateProfile; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public String getCompanyNameRaw() { return companyNameRaw; }
    public void setCompanyNameRaw(String companyNameRaw) { this.companyNameRaw = companyNameRaw; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isCurrent() { return isCurrent; }
    public void setCurrent(boolean current) { isCurrent = current; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
