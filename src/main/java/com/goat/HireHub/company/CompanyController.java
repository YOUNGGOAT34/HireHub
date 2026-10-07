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
     public ResponseEntity<?> getCompanyById(@PathVariable Long id){
          try{
              return ResponseEntity.ok(companyService.getCompanyById(id));
          }catch (RuntimeException e){
               return ResponseEntity.notFound().build();
         }
     }

     @PostMapping
    public ResponseEntity<Company> addCompany(@RequestBody Company company){
          return ResponseEntity.status(HttpStatus.CREATED).body(companyService.addCompany(company));
     }

     @DeleteMapping("/{id}")

     public ResponseEntity<Void> deleteCompany(@PathVariable Long id){
          try{
              companyService.deleteById(id);
              return ResponseEntity.noContent().build();

          } catch (RuntimeException e) {
               return ResponseEntity.notFound().build();
          }
     }

     @PutMapping("/{id}")
    public ResponseEntity<Company> updateCompany(@PathVariable Long id,@RequestBody Company updatedCompany){
          try{
              return ResponseEntity.ok(companyService.updateCompany(id,updatedCompany));

          } catch (RuntimeException e) {
              return ResponseEntity.notFound().build();
          }
     }

     @PatchMapping("/{id}")

     public ResponseEntity<?> patchCompany(@PathVariable Long id,@RequestBody Company updatedCompany){
         try{
             return ResponseEntity.ok(companyService.patchCompany(id,updatedCompany));

         } catch (RuntimeException e) {
             return ResponseEntity.notFound().build();
         }
     }

}
