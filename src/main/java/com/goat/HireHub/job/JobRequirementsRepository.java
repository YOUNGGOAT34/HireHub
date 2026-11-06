package com.goat.HireHub.job;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRequirementsRepository extends JpaRepository<JobRequirement,Long> {
    List<JobRequirement> findByJobIdOrderByDisplayOrderAsc(Long id);
}
