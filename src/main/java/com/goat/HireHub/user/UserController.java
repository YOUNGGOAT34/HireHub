package com.goat.HireHub.user;


import com.goat.HireHub.user.dto.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
          Pageable pageable= PageRequest.of(page,size,Sort.by(validateSortDirection(direction),sortBy));
          return ResponseEntity.ok(userService.getAllUsers(pageable));
      }

      @GetMapping ("/{id}")
      public ResponseEntity<UserResponse> getUserById(@PathVariable @Positive(message="Id must be greater than zero") Long id){
           return ResponseEntity.ok(userService.getUserById(id));
      }

      @GetMapping("/search")
      public ResponseEntity<Page<UserResponse>> searchUsers(@RequestParam(required = false) String firstName,
                                                  @RequestParam(required = false) String lastName,
                                                  @RequestParam(required = false) String username,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero(message = "page number must be zero or greater")int page,
                                                  @RequestParam(defaultValue = "10") @Size(max =50) @Positive(message = "size must be greater than zero")int size){
          Pageable pageable=PageRequest.of(page,size);
           return ResponseEntity.ok(userService.searchUsers(firstName,lastName,username,pageable));
      }

      @DeleteMapping("/me")
      public ResponseEntity<Void> deleteById(){
          userService.deleteUserById();
          return ResponseEntity.noContent().build();
      }

      @PostMapping
    public ResponseEntity<UserResponse> registerUser(@RequestBody @Valid UserRequest userRequest){
           return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(userRequest));
      }

     @PutMapping("/me")
    public   ResponseEntity<UserResponse> updateUser(@RequestBody @Valid UserUpdateRequest userUpdateRequest){
           return ResponseEntity.ok(userService.updateUser(userUpdateRequest));
     }
     @PatchMapping("/me")
     public   ResponseEntity<UserResponse> patchUser(@RequestBody @Valid UserPatchRequest userPatchRequest){
         return ResponseEntity.ok(userService.patchUser(userPatchRequest));
     }



    //experience endpoints
    @PostMapping("/me/experiences")
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

    //education

    @PostMapping("/me/educations")
    public ResponseEntity<EducationResponse> addEducation(@RequestBody @Valid EducationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.addEducation(request));
    }

    @PutMapping("/me/educations/{id}")
    public ResponseEntity<EducationResponse> updateEducation(
            @PathVariable @Positive(message = "Id must be greater than zero") Long id,
            @RequestBody @Valid EducationRequest request) {
        return ResponseEntity.ok(userService.updateEducation(id, request));
    }

    @DeleteMapping("/me/educations/{id}")
    public ResponseEntity<Void> deleteEducation(
            @PathVariable @Positive(message = "Id must be greater than zero") Long id) {
        userService.deleteEducation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/educations")
    public ResponseEntity<List<EducationResponse>> getUserEducations(
            @PathVariable @Positive(message = "Id must be greater than zero") Long id) {

        return ResponseEntity.ok(userService.getAllEducationsById(id));
    }

    //skills

    @GetMapping("/{id}/skills")
    public ResponseEntity<List<SkillResponse>> getUserSkills(
            @PathVariable @Positive(message = "Id must be greater than zero") Long id) {
        return ResponseEntity.ok(userService.getSkillsByProfileId(id));
    }

    @PostMapping("/me/skills")
    public  ResponseEntity<SkillResponse> addSkillToProfile(@RequestBody @Valid SkillRequest request){
           return ResponseEntity.status(HttpStatus.CREATED).body(userService.addSkillToProfile(request));
    }

    @DeleteMapping("/me/skills/{id}")
    public ResponseEntity<Void> removeSkillFromProfile(@PathVariable @Positive(message = "id must be greater than zero")Long id){
           userService.removeSkillFromProfile(id);
           return ResponseEntity.noContent().build();
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


     private Sort.Direction validateSortDirection(String direction){
         return Sort.Direction.fromOptionalString(direction).orElseThrow(() -> new IllegalArgumentException("direction must be asc or desc"));
     }
}
