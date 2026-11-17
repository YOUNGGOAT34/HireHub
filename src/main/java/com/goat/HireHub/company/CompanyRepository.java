package com.goat.HireHub.company;

import com.goat.HireHub.company.dto.CompanyResponseSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    @Query(
            value= """
                    SELECT company_id AS id,company_name AS name
                    FROM(
                       SELECT company_id,company_name
                       FROM companies
                       WHERE LOWER(company_name) LIKE :pattern
                       LIMIT :limit OFFSET :offset
                    )
                    ORDER BY
                        CASE WHEN :sortBy='name' AND :direction='ASC' THEN LOWER(company_name) END ASC,
                        CASE WHEN :sortBy='name' AND :direction='DESC' THEN LOWER(company_name) END DESC,
                        CASE WHEN :sortBy= 'id' AND :direction='ASC' THEN company_id END ASC,
                        CASE WHEN :sortBy='id' AND :direction='DESC' THEN company_id END DESC,
                        LOWER(company_name) ASC
                    
                    """,
            nativeQuery = true
    )
    List<CompanyResponseSummary> searchAndThenSort(
            @Param("pattern") String pattern,
            @Param("sortBy") String sortBy,
            @Param("direction") String direction,
            @Param("limit") int limit,
            @Param("offset") long offset
    );
}
