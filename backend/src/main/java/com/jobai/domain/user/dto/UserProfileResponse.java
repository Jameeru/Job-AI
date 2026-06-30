package com.jobai.domain.user.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for user profile — returned to the React frontend.
 *
 * <p>Sensitive fields (firebaseUid) are omitted. All relationships are
 * embedded to avoid multiple round-trips from the frontend.
 */
@Data
@Builder
public class UserProfileResponse {

    private UUID id;
    private String email;
    private String fullName;
    private String phone;
    private String linkedinUrl;
    private String githubUrl;
    private String portfolioUrl;
    private String collegeName;
    private String degree;
    private String branch;
    private Short graduationYear;
    private BigDecimal cgpa;
    private String city;
    private Integer expectedSalaryMin;
    private Integer expectedSalaryMax;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    private List<SkillResponse> skills;
    private List<ProjectResponse> projects;
    private List<CertificationResponse> certifications;

    // ── Nested response types ─────────────────────────────────────

    @Data
    @Builder
    public static class SkillResponse {
        private UUID id;
        private String skillName;
        private String category;
        private String proficiency;
    }

    @Data
    @Builder
    public static class ProjectResponse {
        private UUID id;
        private String projectName;
        private String description;
        private String techStack;
        private String githubUrl;
        private String liveUrl;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isFeatured;
    }

    @Data
    @Builder
    public static class CertificationResponse {
        private UUID id;
        private String certName;
        private String issuer;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private String credentialUrl;
        private String credentialId;
    }
}
