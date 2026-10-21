package profile;

import com.goat.HireHub.education.Education;
import com.goat.HireHub.experience.Experience;
import com.goat.HireHub.skill.Skill;
import com.goat.HireHub.user.*;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "candidate_profiles")

public class CandidateProfile {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "profile_id")
    private User user;

    @Column(name = "headline")
    private String headline;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;  // The "About Me" section

    @Enumerated(EnumType.STRING)
    @Column(name = "search_status")
    private SearchStatus searchStatus; // e.g., "ACTIVELY_LOOKING", "NOT_LOOKING"

    @OneToMany(mappedBy = "candidateProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations = new java.util.ArrayList<>();


    @OneToMany(mappedBy = "candidateProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Experience> experiences = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "profile_skills",
            joinColumns = @JoinColumn(name = "profile_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new java.util.HashSet<>();


    public CandidateProfile() {}

    public CandidateProfile(User user, String headline, String summary, SearchStatus searchStatus) {
        this.user = user;
        this.headline = headline;
        this.summary = summary;
        this.searchStatus = searchStatus;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public SearchStatus getSearchStatus() { return searchStatus; }
    public void setSearchStatus(SearchStatus searchStatus) { this.searchStatus = searchStatus; }
    public List<Experience> getExperiences() { return experiences; }
    public void setExperiences(List<Experience> experiences) { this.experiences = experiences; }
    public List<Education> getEducations() { return educations; }
    public void setEducations(List<Education> educations) { this.educations = educations; }
    public Set<Skill> getSkills() { return skills; }
    public void setSkills(Set<Skill> skills) { this.skills = skills; }
}
