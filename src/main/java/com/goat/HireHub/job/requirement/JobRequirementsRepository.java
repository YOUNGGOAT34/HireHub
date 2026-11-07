package com.goat.HireHub.job.requirement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRequirementsRepository extends JpaRepository<JobRequirement,Long> {
    List<JobRequirement> findByJobIdOrderByDisplayOrderAsc(Long id);
}
