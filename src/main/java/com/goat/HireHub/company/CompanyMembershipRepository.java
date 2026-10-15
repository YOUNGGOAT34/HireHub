package com.goat.HireHub.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership,Long> {
    Optional<CompanyMembership> findByUserIdAndCompanyId(Long userId,Long companyId);
}
