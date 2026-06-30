package com.jobai.domain.user.service;

import com.google.firebase.auth.FirebaseToken;
import com.jobai.domain.certification.entity.CandidateCertification;
import com.jobai.domain.project.entity.CandidateProject;
import com.jobai.domain.skill.entity.CandidateSkill;
import com.jobai.domain.user.dto.*;
import com.jobai.domain.user.entity.UserProfile;
import com.jobai.domain.user.repository.UserProfileRepository;
import com.jobai.shared.exception.ConflictException;
import com.jobai.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core service for candidate profile management.
 *
 * <p>All methods accept a {@link FirebaseToken} as the identity source — the
 * UID and email are always extracted from the verified token, never from
 * the request body. This prevents identity spoofing.
 *
 * <p>Transactional boundaries: writes use {@code @Transactional},
 * reads use {@code @Transactional(readOnly = true)} for performance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    // ── Create ────────────────────────────────────────────────────

    /**
     * Creates a new user profile on first login.
     *
     * <p>Called automatically by the frontend after Firebase authentication.
     * If a profile already exists for this UID, a {@link ConflictException} is thrown.
     *
     * @param token   verified Firebase ID token
     * @param request additional profile fields from the onboarding form
     * @return the created profile as a response DTO
     */
    @Transactional
    public UserProfileResponse createProfile(FirebaseToken token, CreateUserProfileRequest request) {
        String firebaseUid = token.getUid();
        String email = token.getEmail();

        log.info("Creating profile for Firebase UID: {}", firebaseUid);

        if (userProfileRepository.existsByFirebaseUid(firebaseUid)) {
            throw new ConflictException("A profile already exists for this user. Use PUT /api/profile to update.");
        }

        UserProfile profile = UserProfile.builder()
            .firebaseUid(firebaseUid)
            .email(email)
            .fullName(request.getFullName())
            .phone(request.getPhone())
            .linkedinUrl(request.getLinkedinUrl())
            .githubUrl(request.getGithubUrl())
            .portfolioUrl(request.getPortfolioUrl())
            .collegeName(request.getCollegeName())
            .degree(request.getDegree())
            .branch(request.getBranch())
            .graduationYear(request.getGraduationYear())
            .cgpa(request.getCgpa())
            .city(request.getCity())
            .expectedSalaryMin(request.getExpectedSalaryMin())
            .expectedSalaryMax(request.getExpectedSalaryMax())
            .build();

        // Attach child relationships
        if (request.getSkills() != null) {
            request.getSkills().forEach(s -> profile.addSkill(mapToSkillEntity(s)));
        }
        if (request.getProjects() != null) {
            request.getProjects().forEach(p -> profile.addProject(mapToProjectEntity(p)));
        }
        if (request.getCertifications() != null) {
            request.getCertifications().forEach(c -> profile.addCertification(mapToCertificationEntity(c)));
        }

        UserProfile saved = userProfileRepository.save(profile);
        log.info("Profile created: {}", saved.getId());
        return mapToResponse(saved);
    }

    // ── Read ──────────────────────────────────────────────────────

    /**
     * Retrieves the profile of the authenticated user.
     *
     * @param token verified Firebase ID token
     * @throws ResourceNotFoundException if no profile exists (frontend should call POST first)
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(FirebaseToken token) {
        UserProfile profile = findByFirebaseUidOrThrow(token.getUid());
        return mapToResponse(profile);
    }

    /**
     * Retrieves a profile by its internal UUID. Used by other services (e.g., ResumeBuilder).
     */
    @Transactional(readOnly = true)
    public UserProfile getProfileEntityById(UUID profileId) {
        return userProfileRepository.findById(profileId)
            .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", profileId));
    }

    /**
     * Retrieves the profile entity for use by other services (non-HTTP context).
     */
    @Transactional(readOnly = true)
    public Optional<UserProfile> findByFirebaseUid(String firebaseUid) {
        return userProfileRepository.findByFirebaseUidWithDetails(firebaseUid);
    }

    // ── Update ────────────────────────────────────────────────────

    /**
     * Updates an existing profile (PATCH semantics — null fields are ignored).
     *
     * <p>When {@code skills}, {@code projects}, or {@code certifications} are non-null
     * in the request, the entire collection is replaced (simplest correct approach
     * for a small dataset like a personal profile).
     *
     * @param token   verified Firebase ID token
     * @param request fields to update (null = keep existing value)
     */
    @Transactional
    public UserProfileResponse updateProfile(FirebaseToken token, UpdateUserProfileRequest request) {
        UserProfile profile = findByFirebaseUidOrThrow(token.getUid());

        log.info("Updating profile for Firebase UID: {}", token.getUid());

        // Patch scalar fields — only update if provided
        if (StringUtils.hasText(request.getFullName()))    profile.setFullName(request.getFullName());
        if (StringUtils.hasText(request.getPhone()))       profile.setPhone(request.getPhone());
        if (StringUtils.hasText(request.getLinkedinUrl())) profile.setLinkedinUrl(request.getLinkedinUrl());
        if (StringUtils.hasText(request.getGithubUrl()))   profile.setGithubUrl(request.getGithubUrl());
        if (StringUtils.hasText(request.getPortfolioUrl()))profile.setPortfolioUrl(request.getPortfolioUrl());
        if (StringUtils.hasText(request.getCollegeName())) profile.setCollegeName(request.getCollegeName());
        if (StringUtils.hasText(request.getDegree()))      profile.setDegree(request.getDegree());
        if (StringUtils.hasText(request.getBranch()))      profile.setBranch(request.getBranch());
        if (request.getGraduationYear() != null)           profile.setGraduationYear(request.getGraduationYear());
        if (request.getCgpa() != null)                     profile.setCgpa(request.getCgpa());
        if (StringUtils.hasText(request.getCity()))        profile.setCity(request.getCity());
        if (request.getExpectedSalaryMin() != null)        profile.setExpectedSalaryMin(request.getExpectedSalaryMin());
        if (request.getExpectedSalaryMax() != null)        profile.setExpectedSalaryMax(request.getExpectedSalaryMax());

        // Replace collections if provided
        if (request.getSkills() != null) {
            profile.getSkills().clear();
            request.getSkills().forEach(s -> profile.addSkill(mapToSkillEntity(s)));
        }
        if (request.getProjects() != null) {
            profile.getProjects().clear();
            request.getProjects().forEach(p -> profile.addProject(mapToProjectEntity(p)));
        }
        if (request.getCertifications() != null) {
            profile.getCertifications().clear();
            request.getCertifications().forEach(c -> profile.addCertification(mapToCertificationEntity(c)));
        }

        UserProfile updated = userProfileRepository.save(profile);
        log.info("Profile updated: {}", updated.getId());
        return mapToResponse(updated);
    }

    // ── Delete ────────────────────────────────────────────────────

    /**
     * Soft-deletes the profile by setting {@code isActive = false}.
     * Data is retained for audit purposes.
     */
    @Transactional
    public void deactivateProfile(FirebaseToken token) {
        UserProfile profile = findByFirebaseUidOrThrow(token.getUid());
        profile.setIsActive(false);
        userProfileRepository.save(profile);
        log.info("Profile deactivated: {}", profile.getId());
    }

    // ── Private helpers ───────────────────────────────────────────

    private UserProfile findByFirebaseUidOrThrow(String firebaseUid) {
        return userProfileRepository.findByFirebaseUidWithDetails(firebaseUid)
            .orElseThrow(() -> new ResourceNotFoundException(
                "UserProfile", "firebaseUid", firebaseUid));
    }

    private CandidateSkill mapToSkillEntity(SkillRequest dto) {
        return CandidateSkill.builder()
            .skillName(dto.getSkillName())
            .category(dto.getCategory())
            .proficiency(dto.getProficiency())
            .build();
    }

    private CandidateProject mapToProjectEntity(ProjectRequest dto) {
        return CandidateProject.builder()
            .projectName(dto.getProjectName())
            .description(dto.getDescription())
            .techStack(dto.getTechStack())
            .githubUrl(dto.getGithubUrl())
            .liveUrl(dto.getLiveUrl())
            .startDate(dto.getStartDate())
            .endDate(dto.getEndDate())
            .isFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false)
            .build();
    }

    private CandidateCertification mapToCertificationEntity(CertificationRequest dto) {
        return CandidateCertification.builder()
            .certName(dto.getCertName())
            .issuer(dto.getIssuer())
            .issueDate(dto.getIssueDate())
            .expiryDate(dto.getExpiryDate())
            .credentialUrl(dto.getCredentialUrl())
            .credentialId(dto.getCredentialId())
            .build();
    }

    // ── Mapping to response DTO ───────────────────────────────────

    private UserProfileResponse mapToResponse(UserProfile profile) {
        return UserProfileResponse.builder()
            .id(profile.getId())
            .email(profile.getEmail())
            .fullName(profile.getFullName())
            .phone(profile.getPhone())
            .linkedinUrl(profile.getLinkedinUrl())
            .githubUrl(profile.getGithubUrl())
            .portfolioUrl(profile.getPortfolioUrl())
            .collegeName(profile.getCollegeName())
            .degree(profile.getDegree())
            .branch(profile.getBranch())
            .graduationYear(profile.getGraduationYear())
            .cgpa(profile.getCgpa())
            .city(profile.getCity())
            .expectedSalaryMin(profile.getExpectedSalaryMin())
            .expectedSalaryMax(profile.getExpectedSalaryMax())
            .isActive(profile.getIsActive())
            .createdAt(profile.getCreatedAt())
            .updatedAt(profile.getUpdatedAt())
            .skills(profile.getSkills() == null ? Collections.emptyList() :
                profile.getSkills().stream().map(s -> UserProfileResponse.SkillResponse.builder()
                    .id(s.getId())
                    .skillName(s.getSkillName())
                    .category(s.getCategory())
                    .proficiency(s.getProficiency())
                    .build()).collect(Collectors.toList()))
            .projects(profile.getProjects() == null ? Collections.emptyList() :
                profile.getProjects().stream().map(p -> UserProfileResponse.ProjectResponse.builder()
                    .id(p.getId())
                    .projectName(p.getProjectName())
                    .description(p.getDescription())
                    .techStack(p.getTechStack())
                    .githubUrl(p.getGithubUrl())
                    .liveUrl(p.getLiveUrl())
                    .startDate(p.getStartDate())
                    .endDate(p.getEndDate())
                    .isFeatured(p.getIsFeatured())
                    .build()).collect(Collectors.toList()))
            .certifications(profile.getCertifications() == null ? Collections.emptyList() :
                profile.getCertifications().stream().map(c -> UserProfileResponse.CertificationResponse.builder()
                    .id(c.getId())
                    .certName(c.getCertName())
                    .issuer(c.getIssuer())
                    .issueDate(c.getIssueDate())
                    .expiryDate(c.getExpiryDate())
                    .credentialUrl(c.getCredentialUrl())
                    .credentialId(c.getCredentialId())
                    .build()).collect(Collectors.toList()))
            .build();
    }
}
