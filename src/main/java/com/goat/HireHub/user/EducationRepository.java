package com.goat.HireHub.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByCandidateProfileId(Long profileId);
}