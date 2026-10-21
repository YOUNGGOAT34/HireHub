package com.goat.HireHub.user;

import com.goat.HireHub.education.EducationRepository;
import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.experience.ExperienceRepository;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.skill.Skill;
import com.goat.HireHub.skill.SkillRepository;
import com.goat.HireHub.skill.dto.SkillRequest;
import com.goat.HireHub.skill.dto.SkillResponse;
import com.goat.HireHub.user.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import profile.CandidateProfile;

import java.util.List;

@Service
public class UserService {
     private final UserRepository userRepository;
     private final PasswordEncoder passwordEncoder;
     private final ExperienceRepository experienceRepository;
     private final SecurityUtils securityUtils;
     private final EducationRepository educationRepository;
     private final SkillRepository skillRepository;

     public UserService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        ExperienceRepository experienceRepository,
                        EducationRepository educationRepository,
                        SecurityUtils securityUtils,
                        SkillRepository skillRepository){
          this.userRepository=userRepository;
          this.passwordEncoder=passwordEncoder;
          this.experienceRepository=experienceRepository;
          this.educationRepository=educationRepository;
          this.securityUtils=securityUtils;
          this.skillRepository=skillRepository;
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

     @Transactional
     public void deleteUserById(){
           User user= getCurrentUser();
           userRepository.delete(user);
     }

     @Transactional
     public UserResponse updateUser(UserUpdateRequest userRequest){
          User savedUser= getCurrentUser();
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
     public UserResponse patchUser(UserPatchRequest userRequest){
          User savedUser= getCurrentUser();
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
                   .orElseThrow(()->new ResourceNotFoundException("user",id));
     }








     private User getCurrentUser(){
          return securityUtils.getCurrentUser();
     }



}
