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
     private final EducationRepository educationRepository;

     public UserService(UserRepository userRepository,
                        PasswordEncoder passwordEncoder,
                        ExperienceRepository experienceRepository,
                        EducationRepository educationRepository,
                        SecurityUtils securityUtils){
          this.userRepository=userRepository;
          this.passwordEncoder=passwordEncoder;
          this.experienceRepository=experienceRepository;
          this.educationRepository=educationRepository;
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
           Experience experience=authorizeAction(id,"delete");
           experienceRepository.delete(experience);
     }

     @Transactional
     public ExperienceResponse updateExperience(Long id,ExperienceRequest request){
              Experience savedExperience=authorizeAction(id,"update");
               savedExperience.setJobTitle(request.jobTitle());
               savedExperience.setCurrent(request.isCurrent());
               savedExperience.setStartDate(request.startDate());
               savedExperience.setEndDate(request.endDate());
               savedExperience.setCompanyNameRaw(request.companyName());
               savedExperience.setDescription(request.description());

               return ExperienceResponse.from(savedExperience);

     }

     @Transactional
     public List<EducationResponse> getAllEducationsById(Long id){

          return educationRepository.findByCandidateProfileId(id).stream()
                  .map(EducationResponse::from)
                  .toList();
     }
     
     //education
     @Transactional
     public EducationResponse addEducation(EducationRequest request) {
          User loggedInUser = securityUtils.getCurrentUser();
          CandidateProfile profile = loggedInUser.getCandidateProfile();

          if (profile == null) {
               throw new IllegalStateException("Candidate profile is not initialized for this user account.");
          }
          Education education = getEducation(request, profile);
          return EducationResponse.from(educationRepository.save(education));
     }

     @Transactional
     public EducationResponse updateEducation(Long educationId, EducationRequest request) {

          Education savedEducation = authorizeEducationAction(educationId, "update");

          savedEducation.setInstitutionName(request.institutionName());
          savedEducation.setDegreeType(request.degreeType());
          savedEducation.setFieldOfStudy(request.fieldOfStudy());
          savedEducation.setStartDate(request.startDate());
          savedEducation.setEndDate(request.endDate());
          savedEducation.setCurrent(request.isCurrent());
          savedEducation.setActivitiesAndSocieties(request.activitiesAndSocieties());

          return EducationResponse.from(savedEducation);
     }


     @Transactional
     public void deleteEducationSecurely(Long educationId) {
          Education education = authorizeEducationAction(educationId, "delete");
          educationRepository.delete(education);
     }



     private static Education getEducation(EducationRequest request, CandidateProfile profile) {
          Education education = new Education();
          education.setInstitutionName(request.institutionName());
          education.setDegreeType(request.degreeType());
          education.setFieldOfStudy(request.fieldOfStudy());
          education.setStartDate(request.startDate());
          education.setEndDate(request.endDate());
          education.setCurrent(request.isCurrent());
          education.setActivitiesAndSocieties(request.activitiesAndSocieties());
          education.setCandidateProfile(profile); // Safe foreign key binding
          return education;
     }

     private Experience findOrThrow(Long experienceId){
          return experienceRepository.findById(experienceId)
                  .orElseThrow(()->new ResourceNotFoundException("experience",experienceId));
     }

     private Experience authorizeAction(Long id,String action){
          Experience experience=findOrThrow(id);
          User loggedInUser=securityUtils.getCurrentUser();
          Long experienceOwnerProfileId=experience.getCandidateProfile().getId();
          Long currentUserProfileId=loggedInUser.getCandidateProfile().getId();

          if (!currentUserProfileId.equals(experienceOwnerProfileId)){
               throw new org.springframework.security.access.AccessDeniedException(
                       "You do not have permission to "+action+" this experience record."
               );
          }

          return experience;
     }

     private Education authorizeEducationAction(Long id, String action) {
          Education education = educationRepository.findById(id)
                  .orElseThrow(() -> new RuntimeException("Education timeline record not found with id: " + id));

          User loggedInUser = securityUtils.getCurrentUser();
          Long ownerProfileId = education.getCandidateProfile().getId();
          Long currentUserId = loggedInUser.getCandidateProfile().getId();

          if (!currentUserId.equals(ownerProfileId)) {
               throw new org.springframework.security.access.AccessDeniedException(
                       "You do not have permission to " + action + " this education record."
               );
          }
          return education;
     }



}
