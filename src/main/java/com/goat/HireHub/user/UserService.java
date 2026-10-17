package com.goat.HireHub.user;

import com.goat.HireHub.company.Role;
import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserService {
     private final UserRepository userRepository;
     private final PasswordEncoder passwordEncoder;
     private final ExperienceRepository experienceRepository;
     private final SecurityUtils securityUtils;

     public UserService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        ExperienceRepository experienceRepository,
                        SecurityUtils securityUtils){
          this.userRepository=userRepository;
          this.passwordEncoder=passwordEncoder;
          this.experienceRepository=experienceRepository;
          this.securityUtils=securityUtils;
     }

     //users

     public Page<UserResponse> getAllUsers(Pageable pageable){
          return userRepository.findAll(pageable).map(UserResponse::from);
     }

     public UserResponse getUserById(Long id){
            User user=getById(id);
            return UserResponse.from(user);
     }

     public Page<UserResponse> searchUsers(String firstName,String lastName,String username,Pageable pageable){
          Specification<User> spec=UserSpecification
                  .firstNameMatches(firstName)
                  .and(UserSpecification.lastNameMatches(lastName))
                  .and(UserSpecification.usernameMatches(username));

           return userRepository.findAll(spec,pageable).map(UserResponse::from);
     }

     public void deleteById(Long id){
           if(!userRepository.existsById(id)){
                 throw new ResourceNotFoundException("user not found",id);
           }
           userRepository.deleteById(id);
     }

     @Transactional
     public UserResponse updateUser(Long id, UserUpdateRequest userRequest){
          User savedUser=getById(id);
          String email=userRequest.email().toLowerCase().trim();
          if (!savedUser.getEmail().equals(email) && findByEmail(email)){
               throw new ConflictException("This email address is already taken by another account");
          }
          savedUser.setLastName(userRequest.lastName());
          savedUser.setFirstName(userRequest.firstName());
          savedUser.setUsername(userRequest.username());
          savedUser.setEmail(email);

          return UserResponse.from(savedUser);

     }

     @Transactional
     public UserResponse patchUser(Long id, UserPatchRequest userRequest){
          User savedUser=getById(id);

          if (userRequest.email()!=null){
               String email=userRequest.email().toLowerCase().trim();
               if (!savedUser.getEmail().equals(email) && findByEmail(email)){
                    throw new ConflictException("This email address is already taken by another account");
               }

               savedUser.setEmail(email);
          }

          if(userRequest.lastName()!=null){
               savedUser.setLastName(userRequest.lastName());
          }

          if (userRequest.firstName()!=null){
               savedUser.setFirstName(userRequest.firstName());
          }

          if(userRequest.username()!=null){
               savedUser.setUsername(userRequest.username());

          }

          if (userRequest.password() != null && !userRequest.password().isBlank()) {
               String encodedPassword = passwordEncoder.encode(userRequest.password());
               savedUser.setPasswordHash(encodedPassword);
          }

          return UserResponse.from(savedUser);
     }



     public UserResponse registerUser(UserRequest userRequest){
          String email= userRequest.email().toLowerCase().trim();

          if(findByEmail(email)){
                throw new ConflictException("An account with this email already exists");
          }
          User user=new User();
          user.setEmail(email);
          user.setFirstName(userRequest.firstName());
          user.setLastName(userRequest.lastName());
          user.setUsername(userRequest.username());
          user.setPasswordHash(passwordEncoder.encode(userRequest.password()));
          return  UserResponse.from(userRepository.save(user));
     }

     private Boolean findByEmail(String email){
            return userRepository.existsByEmailIgnoreCase(email);
     }

     private User getById(Long id){
           return userRepository.findById(id)
                   .orElseThrow(()->new ResourceNotFoundException("user not found",id));
     }


     //experience

     public ExperienceResponse addExperience(ExperienceRequest request){
           User user=securityUtils.getCurrentUser();
           CandidateProfile profile=user.getCandidateProfile();

           if (profile== null) {
               throw new IllegalStateException("Candidate profile is not initialized for this user.");
          }

            Experience experience=new Experience();
            experience.setJobTitle(request.jobTitle());
            experience.setCurrent(request.isCurrent());
            experience.setStartDate(request.startDate());
            experience.setEndDate(request.endDate());
            experience.setCompanyNameRaw(request.companyName());
            experience.setDescription(request.description());
            experience.setCandidateProfile(profile);

            return ExperienceResponse.from(experienceRepository.save(experience));
     }


     public List<ExperienceResponse> getAllExperiencesByUserId(Long userId){
           return experienceRepository.findByCandidateProfileId(userId).stream().map(ExperienceResponse::from).toList();
     }

     public void deleteExperienceById(Long id){
           Experience experience=findOrThrow(id);
           User loggedInUser=securityUtils.getCurrentUser();
           Long experienceOwnerProfileId=experience.getCandidateProfile().getId();
           Long currentUserProfileId=loggedInUser.getCandidateProfile().getId();

           if (!currentUserProfileId.equals(experienceOwnerProfileId)){
                throw new org.springframework.security.access.AccessDeniedException(
                        "You do not have permission to delete this experience record."
                );
           }

           experienceRepository.delete(experience);
     }


     private Experience findOrThrow(Long experienceId){
          return experienceRepository.findById(experienceId)
                  .orElseThrow(()->new ResourceNotFoundException("experience",experienceId));
     }

}
