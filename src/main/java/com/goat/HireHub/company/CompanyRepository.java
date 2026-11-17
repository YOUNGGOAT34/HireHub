package com.goat.HireHub.company;

import com.goat.HireHub.company.dto.CompanyResponseSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface CompanyRepository extends JpaRepository<Company,Long>, JpaSpecificationExecutor<Company> {

    @Query(
            """
                SELECT new com.goat.HireHub.company.dto.CompanyResponseSummary(
                    c.id,
                    c.name
                )
                FROM Company c
                   \s"""
    )
    Slice<CompanyResponseSummary> findAllSummaries(Pageable pageable);
}
