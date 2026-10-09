package com.goat.HireHub.job;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job,Long> {
    Page<Job> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
