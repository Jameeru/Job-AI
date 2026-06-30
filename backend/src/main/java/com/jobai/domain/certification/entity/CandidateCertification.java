package com.jobai.domain.certification.entity;

import com.jobai.domain.user.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A real certification or course completion owned by a candidate.
 *
 * <p>Only actual, verifiable certifications should be stored.
 * Examples: AWS Cloud Practitioner, Oracle Java SE, Coursera course with certificate.
 */
@Entity
@Table(name = "candidate_certifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateCertification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @Column(name = "cert_name", nullable = false, length = 255)
    private String certName;

    @Column(name = "issuer", length = 255)
    private String issuer;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "credential_url", length = 500)
    private String credentialUrl;

    @Column(name = "credential_id", length = 255)
    private String credentialId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
