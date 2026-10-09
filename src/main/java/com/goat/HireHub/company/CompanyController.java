package com.goat.HireHub.company;

import com.goat.HireHub.company.dto.CompanyPatchRequest;
import com.goat.HireHub.company.dto.CompanyRequest;
import com.goat.HireHub.company.dto.CompanyResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
@Validated
public class CompanyController {
     private final CompanyService companyService;
     public CompanyController(CompanyService companyService){
          this.companyService=companyService;
     }

     @GetMapping
     public ResponseEntity<Page<CompanyResponse>> getAllCompanies(@RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be 0 or greater") int page,
                                                                  @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be at least 1")
                                                                  @Max(value = 100, message = "Size must not exceed 100")int size){
           Pageable pageable= PageRequest.of(page,size);
           return ResponseEntity.ok(companyService.getAllCompanies(pageable));
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
