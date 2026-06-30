package com.jobai.domain.application.dto;

import com.jobai.domain.application.entity.ApplicationStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * API response for a job application record.
 */
public record ApplicationResponse(
    UUID id,
    UUID jobId,
    UUID resumeVersionId,
    UUID coverLetterId,
    ApplicationStatus status,
    Instant appliedAt,
    Instant interviewDate,
    Integer offerAmount,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {}
