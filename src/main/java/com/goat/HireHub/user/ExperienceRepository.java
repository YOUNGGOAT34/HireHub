package com.goat.HireHub.user;

import com.goat.HireHub.user.dto.ExperienceResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExperienceRepository extends JpaRepository<Experience,Long> {
    List<Experience> findByCandidateProfileId(Long profileId);
}
