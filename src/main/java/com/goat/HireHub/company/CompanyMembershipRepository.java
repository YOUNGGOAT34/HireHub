package com.goat.HireHub.company;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership,Long> {
    Optional<CompanyMembership> findByUserIdAndCompanyId(Long userId,Long companyId);
    @Query(value = "SELECT cm FROM CompanyMembership cm JOIN FETCH cm.user WHERE cm.company.id = :companyId",
            countQuery = "SELECT count(cm) FROM CompanyMembership cm WHERE cm.company.id = :companyId")
    Page<CompanyMembership> findMembershipsByCompanyId(@Param("companyId") Long companyId, Pageable pageable);
}
