package com.goat.HireHub.company;

import com.goat.HireHub.company.dto.CompanyPatchRequest;
import com.goat.HireHub.company.dto.CompanyRequest;
import com.goat.HireHub.company.dto.CompanyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
public class CompanyController {
     private final CompanyService companyService;

     public CompanyController(CompanyService companyService){
          this.companyService=companyService;
     }

     @GetMapping
     public ResponseEntity<List<CompanyResponse>> getAllCompanies(){
           return ResponseEntity.ok(companyService.getAllCompanies());
     }

     @GetMapping("/{id}")
     public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id){
         return ResponseEntity.ok(companyService.getCompanyById(id));
     }

     @PostMapping
    public ResponseEntity<CompanyResponse> addCompany(@Valid @RequestBody CompanyRequest company){
          return ResponseEntity.status(HttpStatus.CREATED).body(companyService.addCompany(company));
     }

     @DeleteMapping("/{id}")

     public ResponseEntity<Void> deleteCompany(@PathVariable Long id){
              companyService.deleteById(id);
              return ResponseEntity.noContent().build();
     }

     @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable Long id,@Valid @RequestBody CompanyRequest updatedCompany){
              return ResponseEntity.ok(companyService.updateCompany(id,updatedCompany));

     }

     @PatchMapping("/{id}")

     public ResponseEntity<CompanyResponse> patchCompany(@PathVariable Long id,@Valid @RequestBody CompanyPatchRequest updatedCompany){
             return ResponseEntity.ok(companyService.patchCompany(id,updatedCompany));
     }

}
