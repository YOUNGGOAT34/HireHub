package com.goat.HireHub.company;

import com.goat.HireHub.company.dto.CompanyMemberResponse;
import com.goat.HireHub.company.dto.CompanyPatchRequest;
import com.goat.HireHub.company.dto.CompanyRequest;
import com.goat.HireHub.company.dto.CompanyResponse;
import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Service
public class CompanyService {
     private final CompanyRepository companyRepository;
     private final CompanyMembershipRepository companyMembershipRepository;
     private final SecurityUtils securityUtils;
     private final UserRepository userRepository;

     public CompanyService(CompanyRepository companyRepository,
                           CompanyMembershipRepository companyMembershipRepository,
                           SecurityUtils securityUtils,
                           UserRepository userRepository){
          this.companyRepository=companyRepository;
          this.companyMembershipRepository=companyMembershipRepository;
          this.securityUtils=securityUtils;
          this.userRepository=userRepository;
     }


     public Page<CompanyResponse> getAllCompanies( Pageable pageable){
         return companyRepository.findAll(pageable).map(CompanyResponse::from);
     }

     public Page<CompanyResponse> searchCompany(String name,Pageable pageable){
         Specification<Company> spec=CompanySpecification.nameContains(name);
         return companyRepository.findAll(spec,pageable).map(CompanyResponse::from);
     }

     public CompanyResponse getCompanyById(Long id){
         Company company=findOrThrow(id);
         return CompanyResponse.from(company);
     }

     @Transactional
     public void deleteById(Long id){
         authorizeAccess(id);
         companyRepository.deleteById(id);
     }

     public Page<CompanyMemberResponse> getMembers(Long id,Pageable pageable){
           findOrThrow(id);
           return companyMembershipRepository.findMembershipsByCompanyId(id,pageable).map(CompanyMemberResponse::from);
     }

     @Transactional
     public CompanyResponse addCompany(CompanyRequest company){
         User currentUser=securityUtils.getCurrentUser();
         Company newCompany=new Company();
         newCompany.setName(company.name());
         newCompany.setDescription(company.description());
         Company savedCompany=companyRepository.save(newCompany);
         CompanyMembership companyMembership=new CompanyMembership(currentUser,savedCompany,Role.ADMIN);
         companyMembershipRepository.save(companyMembership);
         return CompanyResponse.from(savedCompany);
     }

     @Transactional
     public CompanyResponse updateCompany(Long id,CompanyRequest updatedCompany){
         Company savedCompany=authorizeAccess(id);

          savedCompany.setDescription(updatedCompany.description());
          savedCompany.setName(updatedCompany.name());
          return CompanyResponse.from(savedCompany);
     }

     @Transactional
     public  CompanyResponse patchCompany(Long id, CompanyPatchRequest updatedCompany){
         Company savedCompany=authorizeAccess(id);
         if (updatedCompany.name()!=null){
              savedCompany.setName(updatedCompany.name());
         }

         if (updatedCompany.description()!=null){
             savedCompany.setDescription(updatedCompany.description());
         }

         return CompanyResponse.from(savedCompany);
     }

     @Transactional
     public CompanyMemberResponse addMember(Long companyId, String email, Role role){
            Company company=authorizeAccess(companyId);
            User targetUser=userRepository.findByEmailIgnoreCase(email)
                            .orElseThrow(()->new ResourceNotFoundException("user"));
            boolean isMember=companyMembershipRepository.findByUserIdAndCompanyId(targetUser.getId(),companyId).isPresent();
            if(isMember){
                 throw new ConflictException(email+"is already a member of this company");
            }

            CompanyMembership companyMembership=new CompanyMembership(targetUser,company,role);
            return CompanyMemberResponse.from(companyMembershipRepository.save(companyMembership));
     }


    public Company findOrThrow(Long id){
        return companyRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("company",id));
    }

    public CompanyMembership findMemberShipOrThrow(Long userId,Long companyId){
        return companyMembershipRepository.findByUserIdAndCompanyId(userId,companyId)
                .orElseThrow(()-> new org.springframework.security.access.AccessDeniedException(
                        "Access Denied: You do not work for this company."));
    }

    private Company authorizeAccess(Long id){
        Company company=findOrThrow(id);
        User currentUser=securityUtils.getCurrentUser();
        CompanyMembership membership=findMemberShipOrThrow(currentUser.getId(), id);

        if(membership.getRole() != Role.ADMIN){
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access Denied: Only a company admin can delete this company."
            );
        }

        return company;
    }
}
