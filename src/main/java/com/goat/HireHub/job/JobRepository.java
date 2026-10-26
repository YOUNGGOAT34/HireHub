package com.goat.HireHub.job;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface JobRepository extends JpaRepository<Job,Long>, JpaSpecificationExecutor<Job> {
}
