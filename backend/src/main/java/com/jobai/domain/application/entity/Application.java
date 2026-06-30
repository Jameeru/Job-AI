package com.jobai.domain.application.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Tracks a job application — from preparation through offer/rejection.
 *
 * <p>The unique constraint on (user_id, job_id) prevents duplicate applications.
 * Status transitions are recorded in {@link ApplicationStatusHistory}.
 *
 * <p>Uses plain UUID foreign keys to avoid JPA lazy-loading complexity in the service layer.
 */
@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "resume_version_id")
    private UUID resumeVersionId;

    @Column(name = "cover_letter_id")
    private UUID coverLetterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(name = "applied_at")
    private Instant appliedAt;

    @Column(name = "interview_date")
    private Instant interviewDate;

    @Column(name = "offer_amount")
    private Integer offerAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /** Playwright automation session metadata (screenshots, form data) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "playwright_session", columnDefinition = "jsonb")
    private String playwrightSession;

    @Column(name = "screenshot_paths", columnDefinition = "TEXT")
    private String screenshotPaths;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
