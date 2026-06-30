package com.jobai.domain.job.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a scraped job posting from any source platform.
 *
 * <p>The {@code aiAnalysis} field stores the full Claude response as JSONB,
 * allowing flexible querying without schema migrations when the AI output evolves.
 *
 * <p>Jobs with {@code matchScore} < 8.0 are soft-rejected via {@code isRejected}.
 */
@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "external_id", length = 500)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private JobSource source;

    @Column(name = "job_title", nullable = false, length = 255)
    private String jobTitle;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "is_remote", nullable = false)
    @Builder.Default
    private Boolean isRemote = false;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @Column(name = "required_skills", columnDefinition = "TEXT")
    private String requiredSkills;

    @Column(name = "preferred_skills", columnDefinition = "TEXT")
    private String preferredSkills;

    @Column(name = "experience_min")
    private Short experienceMin;

    @Column(name = "experience_max")
    private Short experienceMax;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    @Column(name = "salary_currency", length = 10)
    @Builder.Default
    private String salaryCurrency = "INR";

    @Column(name = "application_url", length = 1000)
    private String applicationUrl;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "match_score", precision = 4, scale = 2)
    private BigDecimal matchScore;

    @Column(name = "is_rejected", nullable = false)
    @Builder.Default
    private Boolean isRejected = false;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /** Full Claude AI analysis stored as JSONB */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ai_analysis", columnDefinition = "jsonb")
    private String aiAnalysis;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "scraped_at", nullable = false)
    @Builder.Default
    private Instant scrapedAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
