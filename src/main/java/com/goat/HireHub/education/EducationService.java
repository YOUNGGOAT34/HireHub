package com.goat.HireHub.education;

import com.goat.HireHub.education.dto.EducationRequest;
import com.goat.HireHub.education.dto.EducationResponse;
import com.goat.HireHub.exception.ResourceNotFoundException;
import com.goat.HireHub.security.SecurityUtils;
import com.goat.HireHub.user.User;
import jakarta.transaction.Transactional;
import profile.CandidateProfile;
import profile.CandidateProfileService;

import java.util.List;

public class EducationService {

    private final SecurityUtils securityUtils;
    private final EducationRepository educationRepository;
    private final CandidateProfileService candidateProfileService;

    public EducationService(EducationRepository educationRepository,SecurityUtils securityUtils,CandidateProfileService candidateProfileService){
         this.educationRepository=educationRepository;
         this.securityUtils=securityUtils;
         this.candidateProfileService=candidateProfileService;
    }

    //education
    @Transactional
    public EducationResponse addEducation(EducationRequest request) {
        User loggedInUser =getCurrentUser();
        CandidateProfile profile =candidateProfileService.getOrCreateCandidateProfile(loggedInUser);
        Education education = getEducation(request, profile);
        return EducationResponse.from(educationRepository.save(education));
    }

    @Transactional
    public List<EducationResponse> getAllEducationsById(Long id){

        return educationRepository.findByCandidateProfileId(id).stream()
                .sorted((a,b)->b.getStartDate().compareTo(a.getStartDate()))
                .map(EducationResponse::from)
                .toList();
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
    public void deleteEducation(Long educationId) {
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
        education.setCandidateProfile(profile);
        return education;
    }

    private Education authorizeEducationAction(Long id, String action) {
        Education education = educationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Education timeline record not found with id: " + id));

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

    private User getCurrentUser(){
        return securityUtils.getCurrentUser();
    }
}
