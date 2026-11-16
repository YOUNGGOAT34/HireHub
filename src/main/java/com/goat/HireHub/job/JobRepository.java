package com.goat.HireHub.job;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface JobRepository extends JpaRepository<Job,Long>, JpaSpecificationExecutor<Job> {
    @Override
    @EntityGraph(attributePaths = {"company"})
    Optional<Job> findById(Long id);
    @EntityGraph(attributePaths = {"company"})
    Slice<Job> findAllBy(Pageable pageable);
}
