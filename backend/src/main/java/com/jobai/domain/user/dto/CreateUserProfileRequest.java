package com.jobai.domain.user.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating a user profile on first login.
 *
 * <p>The {@code firebaseUid} and {@code email} are extracted from the
 * verified Firebase token — they are NOT accepted from the request body.
 * Only additional profile fields are provided here.
 */
@Data
@Builder
public class CreateUserProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phone;

    @Size(max = 500)
    private String linkedinUrl;

    @Size(max = 500)
    private String githubUrl;

    @Size(max = 500)
    private String portfolioUrl;

    @Size(max = 255)
    private String collegeName;

    @Size(max = 100)
    private String degree;

    @Size(max = 100)
    private String branch;

    @Min(2020) @Max(2030)
    private Short graduationYear;

    @DecimalMin("0.0") @DecimalMax("10.0")
    private BigDecimal cgpa;

    @Size(max = 100)
    private String city;

    @Min(0)
    private Integer expectedSalaryMin;

    @Min(0)
    private Integer expectedSalaryMax;

    private List<SkillRequest> skills;
    private List<ProjectRequest> projects;
    private List<CertificationRequest> certifications;
}
