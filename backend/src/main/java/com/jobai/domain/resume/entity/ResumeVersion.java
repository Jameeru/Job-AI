package com.jobai.domain.resume.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A generated, ATS-optimized resume tailored for a specific job.
 *
 * <p>Each job gets its own resume version with targeted keywords.
 * The HTML content is the source of truth; the PDF is generated from it.
 *
 * <p>Uses plain UUID foreign keys instead of JPA relationships to avoid
 * eager/lazy loading complexity in the service layer.
 */
@Entity
@Table(name = "resume_versions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "version_label", length = 100)
    private String versionLabel;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    @Column(name = "keywords_used", columnDefinition = "TEXT")
    private String keywordsUsed;

    @Column(name = "ats_score", precision = 4, scale = 2)
    private BigDecimal atsScore;

    @Column(name = "generated_at", nullable = false)
    @Builder.Default
    private Instant generatedAt = Instant.now();
}
