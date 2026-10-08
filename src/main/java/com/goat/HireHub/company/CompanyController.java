package com.goat.HireHub.company;

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
     public ResponseEntity<List<Company>> getAllCompanies(){
           return ResponseEntity.ok(companyService.getAllCompanies());
     }

     @GetMapping("/{id}")
     public ResponseEntity<Company> getCompanyById(@PathVariable Long id){
         return ResponseEntity.ok(companyService.getCompanyById(id));
     }

     @PostMapping
    public ResponseEntity<Company> addCompany(@RequestBody Company company){
          return ResponseEntity.status(HttpStatus.CREATED).body(companyService.addCompany(company));
     }

     @DeleteMapping("/{id}")

     public ResponseEntity<Void> deleteCompany(@PathVariable Long id){
              companyService.deleteById(id);
              return ResponseEntity.noContent().build();
     }

     @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id,@RequestBody Company updatedCompany){
              return ResponseEntity.ok(companyService.updateCompany(id,updatedCompany));

     }

     @PatchMapping("/{id}")

     public ResponseEntity<Company> patchCompany(@PathVariable Long id,@RequestBody Company updatedCompany){
             return ResponseEntity.ok(companyService.patchCompany(id,updatedCompany));
     }

}
