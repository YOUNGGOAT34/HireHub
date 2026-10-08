package com.goat.HireHub.company;

import com.goat.HireHub.company.dto.CompanyPatchRequest;
import com.goat.HireHub.company.dto.CompanyRequest;
import com.goat.HireHub.company.dto.CompanyResponse;
import com.goat.HireHub.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyService {
     private final CompanyRepository companyRepository;

     public CompanyService(CompanyRepository companyRepository){
          this.companyRepository=companyRepository;
     }

    private Company findOrThrow(Long id){
        return companyRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("company not found",id));
    }

     public List<CompanyResponse> getAllCompanies(){
         return companyRepository.findAll().stream()
                 .map(CompanyResponse::from)
                 .toList();
     }

     public CompanyResponse getCompanyById(Long id){
         Company company=findOrThrow(id);
         return CompanyResponse.from(company);
     }

     public void deleteById(Long id){
          if(companyRepository.existsById(id)){
              companyRepository.deleteById(id);
              return;
          }

          throw new ResourceNotFoundException("company not found",id);
     }

     public CompanyResponse addCompany(CompanyRequest company){
         Company saved=new Company();
         saved.setName(company.name());
         saved.setDescription(company.description());

          return CompanyResponse.from(companyRepository.save(saved));
     }

     @Transactional
     public CompanyResponse updateCompany(Long id,CompanyRequest updatedCompany){
          Company savedCompany=findOrThrow(id);

          savedCompany.setDescription(updatedCompany.description());
          savedCompany.setName(updatedCompany.name());
          return CompanyResponse.from(savedCompany);
     }

     @Transactional
     public  CompanyResponse patchCompany(Long id, CompanyPatchRequest updatedCompany){
         Company savedCompany=findOrThrow(id);

         if (updatedCompany.name()!=null){
              savedCompany.setName(updatedCompany.name());
         }

         if (updatedCompany.description()!=null){
             savedCompany.setDescription(updatedCompany.description());
         }

         return CompanyResponse.from(savedCompany);
     }
}
