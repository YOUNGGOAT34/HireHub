package com.goat.HireHub.company;

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

     public List<Company> getAllCompanies(){
          return companyRepository.findAll();
     }

     public Company getCompanyById(Long id){
         return companyRepository.findById(id)
                 .orElseThrow(()->new ResourceNotFoundException("company not found",id));
     }

     public void deleteById(Long id){
          if(companyRepository.existsById(id)){
              companyRepository.deleteById(id);
              return;
          }

          throw new ResourceNotFoundException("company not found",id);
     }

     public Company addCompany(Company company){
          return companyRepository.save(company);
     }

     @Transactional
     public Company updateCompany(Long id,Company updatedCompany){
          Company savedCompany=getCompanyById(id);

          savedCompany.setDescription(updatedCompany.getDescription());
          savedCompany.setName(updatedCompany.getName());

          return savedCompany;
     }

     @Transactional
     public  Company patchCompany(Long id,Company updatedCompany){
         Company savedCompany=getCompanyById(id);

         if (updatedCompany.getName()!=null){
              savedCompany.setName(updatedCompany.getName());
         }

         if (updatedCompany.getDescription()!=null){
             savedCompany.setDescription(updatedCompany.getDescription());
         }

         return savedCompany;
     }
}
