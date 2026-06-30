package com.jobai.domain.resume.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request to generate a tailored resume for a specific job.
 *
 * @param jobId the UUID of the job to tailor the resume for
 */
public record ResumeGenerateRequest(
    @NotNull(message = "jobId is required")
    UUID jobId
) {}
