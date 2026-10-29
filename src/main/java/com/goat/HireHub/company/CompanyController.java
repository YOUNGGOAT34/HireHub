package com.goat.HireHub.company;

import com.goat.HireHub.company.dto.*;
import com.goat.HireHub.exception.BadRequestException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.hibernate.query.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/companies")
@Validated
public class CompanyController {
     private final CompanyService companyService;
     public CompanyController(CompanyService companyService){
          this.companyService=companyService;
     }

     @GetMapping
     public ResponseEntity<Page<CompanyResponse>> getAllCompanies(@RequestParam(defaultValue = "0") @PositiveOrZero(message = "Page must be 0 or greater") int page,
                                                                  @RequestParam(defaultValue = "10") @Positive(message = "Size must be at least 1")
                                                                  @Max(value = 100, message = "Size must not exceed 100")int size,
                                                                  @RequestParam(defaultValue = "name") String sortBy,
                                                                  @RequestParam (defaultValue = "desc") String direction){
         validateSortField(sortBy);
         Sort.Direction sortDirection= Sort.Direction.fromString(direction);
         Pageable pageable=PageRequest.of(page,size,Sort.by(sortDirection,sortBy));
         return ResponseEntity.ok(companyService.getAllCompanies(pageable));
     }

     @GetMapping("/search")
     public ResponseEntity<Page<CompanyResponse>> searchCompany(@RequestParam(defaultValue = "0") @PositiveOrZero(message="Page must be 0 or greater")int page,
                                                                @RequestParam(defaultValue = "10")
                                                                @Max(value=50,message = "size cannot exceed 50")
                                                                @Positive(message = "Size must be at least 1")int size,
                                                                @RequestParam(defaultValue = "name") String sortBy,
                                                                @RequestParam(required = false) String name,
                                                                @RequestParam (defaultValue = "desc") String direction){
         validateSortField(sortBy);
         Sort.Direction sortDirection= Sort.Direction.fromString(direction);
         Pageable pageable=PageRequest.of(page,size,Sort.by(sortDirection,sortBy));
         return ResponseEntity.ok(companyService.searchCompany(name,pageable));
     }

     @GetMapping("/{id}")
     public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable @Positive(message="Id must be greater than zero") Long id){
         return ResponseEntity.ok(companyService.getCompanyById(id));
     }

     @GetMapping("/{id}/members")
     public ResponseEntity<Page<CompanyMemberResponse>> getAllMembers(@PathVariable @Positive(message = "company id must be greater than 0") Long id){
           Pageable pageable=PageRequest.of(0,10);
           return ResponseEntity.ok(companyService.getMembers(id,pageable));
     }

     @PostMapping
    public ResponseEntity<CompanyResponse> addCompany(@Valid @RequestBody CompanyRequest company){
          return ResponseEntity.status(HttpStatus.CREATED).body(companyService.addCompany(company));
     }

     @PostMapping("/{id}/members")
     public ResponseEntity<CompanyMemberResponse> addMember(
             @PathVariable @Positive(message ="company id has to be greater than zero")Long id,
             @Valid @RequestBody MemberInviteRequest request){

          return ResponseEntity.status(HttpStatus.CREATED).body(companyService.addMember(id,request.email(),request.role()));
     }

     @DeleteMapping("/{id}")
     public ResponseEntity<Void> deleteCompany(@PathVariable @Positive(message="Id must be greater than zero") Long id){
              companyService.deleteById(id);
              return ResponseEntity.noContent().build();
     }

     @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable @Positive(message="Id must be greater than zero") Long id,@Valid @RequestBody CompanyRequest updatedCompany){
              return ResponseEntity.ok(companyService.updateCompany(id,updatedCompany));

     }

     @PatchMapping("/{id}")
     public ResponseEntity<CompanyResponse> patchCompany(@PathVariable @Positive(message="Id must be greater than zero") Long id,@Valid @RequestBody CompanyPatchRequest updatedCompany){
             return ResponseEntity.ok(companyService.patchCompany(id,updatedCompany));
     }

     private void validateSortField(String fieldName){
         Set<String> allowedSortFields=Set.of("name");
         if (!allowedSortFields.contains(fieldName)) {
             throw new BadRequestException(
                     "Invalid sort field: " + fieldName
             );
         }
     }

     private Sort.Direction validateSortDirection(String direction){
         Set<String> allowedSortFields=Set.of("desc","asc");
         if (!allowedSortFields.contains(direction)) {
             throw new BadRequestException(
                     "Invalid sort field: " + direction
             );
         }
         return Sort.Direction.fromString(direction);
     }

}
