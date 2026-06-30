package com.jobai.domain.coverletter.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request to generate a cover letter for a specific job.
 *
 * @param jobId the UUID of the target job
 */
public record CoverLetterGenerateRequest(
    @NotNull(message = "jobId is required")
    UUID jobId
) {}
