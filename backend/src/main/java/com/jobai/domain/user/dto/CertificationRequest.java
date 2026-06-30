package com.jobai.domain.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Sub-DTO for certification data inside profile requests.
 */
@Data
@Builder
public class CertificationRequest {
    private String certName;
    private String issuer;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String credentialUrl;
    private String credentialId;
}
