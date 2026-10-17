package com.goat.HireHub.user;


import com.goat.HireHub.user.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
@RequestMapping("/users")
@Validated
public class UserController {
      private final UserService userService;

      public UserController(UserService userService){
           this.userService=userService;
      }

      @GetMapping
      public ResponseEntity<Page<UserResponse>> getUsers(
              @RequestParam(defaultValue = "0") @PositiveOrZero(message = "Page number has to be zero or greater") int page,
              @RequestParam(defaultValue = "10") @Positive(message = "Page size must greater than zero") int size,
              @RequestParam(defaultValue = "firstName")String sortBy,
              @RequestParam(defaultValue = "desc") String direction){

          validateSortingField(sortBy);
          Sort.Direction sortDirection=Sort.Direction.fromString(direction);
          Pageable pageable= PageRequest.of(page,size,Sort.by(sortDirection,sortBy));
          return ResponseEntity.ok(userService.getAllUsers(pageable));
      }

      @GetMapping ("/{id}")
      public ResponseEntity<UserResponse> getUserById(@PathVariable @Positive(message="Id must be greater than zero") Long id){
           return ResponseEntity.ok(userService.getUserById(id));
      }

      @GetMapping("/search")
      public ResponseEntity<Page<UserResponse>> searchByFirstName(@RequestParam(required = false) String firstName,
                                                  @RequestParam(required = false) String lastName,
                                                  @RequestParam(required = false) String username,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero(message = "page number must be zero or greater")int page,
                                                  @RequestParam(defaultValue = "10") @Positive(message = "size must be greater than zero")int size){
          Pageable pageable=PageRequest.of(page,size);
           return ResponseEntity.ok(userService.searchUsers(firstName,lastName,username,pageable));
      }

      @DeleteMapping("/{id}")
      public ResponseEntity<Void> deleteById(@PathVariable @Positive(message="Id must be greater than zero") Long id){
          userService.deleteById(id);
          return ResponseEntity.noContent().build();
      }

      @PostMapping
    public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRequest userRequest){
           return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userRequest));
      }

     @PutMapping("/{id}")
    public   ResponseEntity<UserResponse> updateUser(@PathVariable @Positive(message="Id must be greater than zero") Long id, @RequestBody @Valid UserUpdateRequest userUpdateRequest){
           return ResponseEntity.ok(userService.updateUser(id,userUpdateRequest));
     }
     @PatchMapping("/{id}")
     public   ResponseEntity<UserResponse> patchUser(@PathVariable @Positive(message="Id must be greater than zero") Long id, @RequestBody @Valid UserPatchRequest userPatchRequest){
         return ResponseEntity.ok(userService.patchUser(id,userPatchRequest));
     }

    //experience endpoints
    @PostMapping("/me/experience")
    public ResponseEntity<ExperienceResponse> addExperience(
            @RequestBody @Valid ExperienceRequest experienceRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addExperience(experienceRequest));
    }

    @GetMapping("/{id}/experiences")
    public ResponseEntity<List<ExperienceResponse>> getUserExperiences(
            @PathVariable @Positive(message = "Id must be greater than zero") Long id) {
        List<ExperienceResponse> responses = userService.getAllExperiencesByUserId(id);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/me/experiences/{id}")
    public ResponseEntity<Void> deleteExperience(@PathVariable @Positive(message = "Id must be greater than zero") Long id){
             userService.deleteExperienceById(id);
             return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/experiences/{id}")
    public ResponseEntity<ExperienceResponse> updateExperience(@PathVariable @Positive(message = "id must be greater than zero")Long id,@RequestBody @Valid ExperienceRequest request){
           return ResponseEntity.ok(userService.updateExperience(id,request));
    }

     //helpers
     private void validateSortingField(String field){
         Set<String> allowedSortFields=Set.of("firstName","lastName","email","username");
         if (!allowedSortFields.contains(field)) {
             throw new IllegalArgumentException(
                     "Invalid sort field: " + field
             );
         }
     }



}
