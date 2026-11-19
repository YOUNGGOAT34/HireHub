package com.goat.HireHub.company;

import com.goat.HireHub.company.dto.*;
import com.goat.HireHub.company.membership.dto.CompanyMemberResponse;
import com.goat.HireHub.company.membership.CompanyMembership;
import com.goat.HireHub.company.membership.CompanyMembershipRepository;
import com.goat.HireHub.company.membership.MemberAddedOrDeletedEvent;
import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.AuthenticatedUser;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import com.goat.HireHub.user.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CompanyService {
     private final CompanyRepository companyRepository;
     private final CompanyMembershipRepository companyMembershipRepository;
     private final SecurityUtils securityUtils;
     private final UserRepository userRepository;
    private final ApplicationEventPublisher publisher;

     public CompanyService(CompanyRepository companyRepository,
                           CompanyMembershipRepository companyMembershipRepository,
                           SecurityUtils securityUtils,
                           UserRepository userRepository,
                           ApplicationEventPublisher publisher
                           ){
          this.companyRepository=companyRepository;
          this.companyMembershipRepository=companyMembershipRepository;
          this.securityUtils=securityUtils;
          this.userRepository=userRepository;
          this.publisher=publisher;
     }

     @Cacheable(value="companies",key="{#pageable.pageNumber,#pageable.pageSize,#pageable.sort}")
     public Slice<CompanyResponseSummary> getAllCompanies(Pageable pageable){
         return companyRepository.findAllSummaries(pageable);
     }

     @Cacheable(value="companySearch",key="{#name?.trim()?.toLowerCase(),#pageable.pageNumber,#pageable.pageSize,#pageable.sort}")
     public Slice<CompanyResponseSummary> searchCompany(String name,Pageable pageable){
         String keyword=(name==null)?"":name.trim();
         String escaped = keyword
                 .replace("\\", "\\\\")
                 .replace("%", "\\%")
                 .replace("_", "\\_");

         String pattern = "%" + escaped + "%";

         int pageSize= pageable.getPageSize();
         Sort.Order order=pageable.getSort().iterator().next();
         String direction=order.isDescending()?"DESC":"ASC";
         String sortBy=switch(order.getProperty()){
             case "id"->"id";
             default -> "name";
         };

         List<CompanyResponseSummary> results=companyRepository.searchAndThenSort(pattern,sortBy,direction,pageSize+1,pageable.getOffset());

         boolean hasNext=results.size()>pageSize;

         if(hasNext){
             results=results.subList(0,pageSize);
         }

         return new SliceImpl<>(results,pageable,hasNext);

     }
     @Cacheable(value="company",key = "#id")
     public CompanyResponse getCompanyById(Long id){
         Company company=findOrThrow(id);
         return CompanyResponse.from(company);
     }
     @Transactional
     @Caching(
         evict={
                 @CacheEvict(value={"companies","companySearch"},allEntries = true),
                 @CacheEvict(value="company",key="#id")
         }

     )
     public void deleteById(Long id){
         authorizeAccess(id);
         companyRepository.deleteById(id);
         publisher.publishEvent(new MemberAddedOrDeletedEvent(id));
     }

     @Transactional
     @CacheEvict(value={"companies","companySearch"},allEntries = true)
     public CompanyResponse addCompany(CompanyRequest company){
         Long currentUserId=getCurrentUser().id();
         User userProxy=userRepository.getReferenceById(currentUserId);
         Company newCompany=new Company();
         newCompany.setName(company.name());
         newCompany.setDescription(company.description());
         Company savedCompany=companyRepository.save(newCompany);
         CompanyMembership companyMembership=new CompanyMembership(userProxy,savedCompany,Role.ADMIN);
         companyMembershipRepository.save(companyMembership);
         return CompanyResponse.from(savedCompany);
     }

     @Transactional
     @Caching(
            evict = {
                    @CacheEvict(value={"companies","companySearch"},allEntries = true)
            },
             put=@CachePut(value="company",key="#id")
     )

     public CompanyResponse updateCompany(Long id,CompanyRequest updatedCompany){
         authorizeAccess(id);
         Company savedCompany=findOrThrow(id);
          savedCompany.setDescription(updatedCompany.description());
          savedCompany.setName(updatedCompany.name());
          return CompanyResponse.from(savedCompany);
     }

     @Transactional
     @Caching(
         evict=@CacheEvict(value="companies",allEntries = true),
         put=@CachePut(value="company",key="#id")

     )
     public  CompanyResponse patchCompany(Long id, CompanyPatchRequest updatedCompany){
         authorizeAccess(id);
         Company savedCompany=findOrThrow(id);
         if (updatedCompany.name()!=null){
              savedCompany.setName(updatedCompany.name());
         }

         if (updatedCompany.description()!=null){
             savedCompany.setDescription(updatedCompany.description());
         }

         return CompanyResponse.from(savedCompany);
     }

     //company members
    @Cacheable(value="companyMembers",key="{#id,#pageable.pageNumber,#pageable.pageSize,#pageable.sort}")
    public Slice<CompanyMemberResponse> getMembers(Long id,Pageable pageable){

        Slice<CompanyMemberResponse> members=companyMembershipRepository.findMembershipsByCompanyId(id,pageable);
        if(members.isEmpty() && !companyRepository.existsById(id)){
            throw new ResourceNotFoundException("company",id);
        }
        return members;
    }

     @Transactional
     public CompanyMemberResponse addMember(Long companyId, String email, Role role){
            authorizeAccess(companyId);
            UserSummary targetUser=userRepository.findByEmailIgnoreCase(email)
                            .orElseThrow(()->new ResourceNotFoundException("user"));

            CompanyMembership companyMembership=new CompanyMembership(
                    userRepository.getReferenceById(targetUser.getId()),
                    companyRepository.getReferenceById(companyId)
                    ,role);
         try{
             companyMembershipRepository.saveAndFlush(companyMembership);
         }catch(DataIntegrityViolationException e){

             throw new ConflictException(email+" is already a member of this company");
         }
            publisher.publishEvent(new MemberAddedOrDeletedEvent(companyId));
            return new CompanyMemberResponse(targetUser.getId(),targetUser.getFirstName(),targetUser.getLastName(),role);
     }

     //change role here
    @Transactional
    public void changeRoleById(Long userId,Long companyId,Role newRole){
         authorizeAccess(companyId);
         if(companyMembershipRepository.updateMembershipRole(userId,companyId, newRole.name())==0){
             throw new ResourceNotFoundException("member",userId);

         }
         publisher.publishEvent(new MemberAddedOrDeletedEvent(companyId));

    }

     @Transactional
     public void deleteMemberById(Long userId,Long companyId){
          authorizeAccess(companyId);
          if(companyMembershipRepository.deleteByUserIdAndCompanyId(userId,companyId)==0){
              Role role=companyMembershipRepository.findRoleByUserIdAndCompanyId(userId,companyId).orElseThrow(
                      ()->new ResourceNotFoundException("member not found",userId)
              );


                  throw new ConflictException("Admins cannot be removed,change their role first");

          }
          publisher.publishEvent(new MemberAddedOrDeletedEvent(companyId));
     }

    public Company findOrThrow(Long id){
        return companyRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("company",id));
    }

    public Role findRole(Long userId,Long companyId){
         return companyMembershipRepository.findRoleByUserIdAndCompanyId(userId,companyId).orElse(null);
    }

    private void authorizeAccess(Long companyId){

        Long currentUserId= getCurrentUser().id();
        Role role=companyMembershipRepository.findRoleByUserIdAndCompanyId(currentUserId,companyId).orElse(null);
        if(role==null){
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access Denied: You do not work for this company.");
        }
        if(role != Role.ADMIN){
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access Denied: Only a company admins can perform this action."
            );
        }

    }

    private AuthenticatedUser getCurrentUser(){
         return securityUtils.getCurrentUser();
    }
}
