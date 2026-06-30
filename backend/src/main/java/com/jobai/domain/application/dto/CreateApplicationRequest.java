package com.jobai.domain.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request to track a new job application.
 *
 * @param jobId           the job being applied for (required)
 * @param resumeVersionId the resume version used (optional)
 * @param coverLetterId   the cover letter used (optional)
 * @param notes           any notes about the application (optional)
 */
public record CreateApplicationRequest(
    @NotNull(message = "jobId is required")
    UUID jobId,
    UUID resumeVersionId,
    UUID coverLetterId,
    String notes
) {}
