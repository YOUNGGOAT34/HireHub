package com.goat.HireHub.user;

import com.goat.HireHub.exception.ConflictException;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
     public void deleteUserById(Long id){
           User user= authorizeSelfAction(id,"delete");

           userRepository.delete(user);
     }

     @Transactional
     public UserResponse updateUser(Long id, UserUpdateRequest userRequest){
          User savedUser= authorizeSelfAction(id,"update");
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
          User savedUser= authorizeSelfAction(id,"patch");
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

     //profile
     @Transactional
     public FullProfileResponse getFullProfileByUserId(Long userId) {
          User user = userRepository.findById(userId)
                  .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

          CandidateProfile profile = user.getCandidateProfile();
          List<ExperienceResponse> expList = (profile != null)
                  ? profile.getExperiences().stream().map(ExperienceResponse::from).toList() : List.of();
          List<EducationResponse> eduList = (profile != null)
                  ? profile.getEducations().stream().map(EducationResponse::from).toList() : List.of();

          return new FullProfileResponse(
                  user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), user.getUsername(),
                  (profile != null) ? profile.getHeadline() : null,
                  (profile != null) ? profile.getSummary() : null,
                  (profile != null) ? profile.getSearchStatus() : null,
                  expList, eduList
          );
     }

     @Transactional
     public ProfileSummaryResponse updateMainProfile(String headline, String summary, String searchStatus) {
          User loggedInUser = securityUtils.getCurrentUser();
          CandidateProfile profile = loggedInUser.getCandidateProfile();

          if (profile == null) {
               profile = new CandidateProfile(loggedInUser, headline, summary, searchStatus);
               loggedInUser.setCandidateProfile(profile);
          } else {
               profile.setHeadline(headline);
               profile.setSummary(summary);
               profile.setSearchStatus(searchStatus);
          }

          userRepository.save(loggedInUser);
          return new ProfileSummaryResponse(profile.getId(), profile.getHeadline(), profile.getSummary(), profile.getSearchStatus());
     }


     //experience
     @Transactional
     public ExperienceResponse addExperience(ExperienceRequest request){
           User user=securityUtils.getCurrentUser();
           CandidateProfile profile=user.getCandidateProfile();

           if (profile== null) {
                profile = new CandidateProfile();
                profile.setUser(user);
                profile.setSearchStatus("OPEN_TO_WORK");
                profile.setHeadline("Software Professional");
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
           return experienceRepository.findByCandidateProfileId(userId).stream()
                   .sorted((a, b) -> b.getStartDate().compareTo(a.getStartDate()))
                   .map(ExperienceResponse::from).toList();
     }

     public void deleteExperienceById(Long id){
           Experience experience= authorizeExperienceAction(id,"delete");
           experienceRepository.delete(experience);
     }

     @Transactional
     public ExperienceResponse updateExperience(Long id,ExperienceRequest request){
              Experience savedExperience= authorizeExperienceAction(id,"update");
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
                  .sorted((a,b)->b.getStartDate().compareTo(a.getStartDate()))
                  .map(EducationResponse::from)
                  .toList();
     }
     
     //education
     @Transactional
     public EducationResponse addEducation(EducationRequest request) {
          User loggedInUser = securityUtils.getCurrentUser();
          CandidateProfile profile = loggedInUser.getCandidateProfile();

          if (profile == null) {
               profile = new CandidateProfile();
               profile.setUser(loggedInUser);
               profile.setSearchStatus("OPEN_TO_WORK");
               profile.setHeadline("Software Professional");
               userRepository.save(loggedInUser);
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

     //skills
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
     public SkillResponse addSkillToProfile(SkillRequest skillRequest){

          User loggedInUser = securityUtils.getCurrentUser();
          CandidateProfile profile = loggedInUser.getCandidateProfile();

          if (profile == null) {
               profile = new CandidateProfile();
               profile.setUser(loggedInUser);
               profile.setSearchStatus("OPEN_TO_WORK");
               profile.setHeadline("Software Professional");
               loggedInUser.setCandidateProfile(profile);
               userRepository.save(loggedInUser);
          }

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


     private Experience findOrThrow(Long experienceId){
          return experienceRepository.findById(experienceId)
                  .orElseThrow(()->new ResourceNotFoundException("experience",experienceId));
     }

     private User authorizeSelfAction(Long userId, String action){
          User loggedInUser=securityUtils.getCurrentUser();
          if(!Objects.equals(loggedInUser.getId(), userId)){
               throw new org.springframework.security.access.AccessDeniedException(
                       "You do not have permission to "+action+" this user."
               );
          }
          return loggedInUser;
     }

     private Experience authorizeExperienceAction(Long id, String action){
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

     private Skill findOrThrow(String name){
           return skillRepository.findByNameIgnoreCase(name)
                   .orElseThrow(()->new ResourceNotFoundException("skill"));
     }


}
