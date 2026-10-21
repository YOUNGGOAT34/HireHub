package com.goat.HireHub.user;

import com.goat.HireHub.education.Education;
import com.goat.HireHub.education.EducationRepository;
import com.goat.HireHub.education.dto.EducationRequest;
import com.goat.HireHub.education.dto.EducationResponse;
import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.experience.ExperienceRepository;
import com.goat.HireHub.security.SecurityUtils;
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




     //skills
     @Transactional
     public List<SkillResponse> getSkillsByProfileId(Long userId){
          if (!userRepository.existsById(userId)) {
               throw new ResourceNotFoundException("User not found with id", userId);
          }

          return skillRepository.findAllByProfileId(userId)
                  .stream()
                  .map(SkillResponse::from)
                  .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                  .toList();
     }
     @Transactional
     public SkillResponse addSkillToProfile(SkillRequest skillRequest){

          User loggedInUser = securityUtils.getCurrentUser();
          CandidateProfile profile = getOrCreateCandidateProfile(loggedInUser);

          String cleanSkillName=skillRequest.name().trim();

          Skill skill = skillRepository.findByNameIgnoreCase(cleanSkillName)
                  .orElseGet(() -> skillRepository.save(new Skill(cleanSkillName)));

          if (profile.getSkills().contains(skill)) {
               throw new ConflictException("You have already added the skill '" + cleanSkillName + "' to your profile.");
          }
          profile.getSkills().add(skill);
             return SkillResponse.from(skillRepository.save(skill));
     }

     public void removeSkillFromProfile(Long skillId){
          User loggedInUser = securityUtils.getCurrentUser();
          CandidateProfile profile = loggedInUser.getCandidateProfile();

          if (profile == null) {
               throw new ResourceNotFoundException("Profile not found for current session user.");
          }

          Skill skill = skillRepository.findById(skillId)
                  .orElseThrow(() -> new ResourceNotFoundException("Skill record not found in system taxonomy registry", skillId));

          profile.getSkills().remove(skill);
     }



     private User getCurrentUser(){
          return securityUtils.getCurrentUser();
     }







}
