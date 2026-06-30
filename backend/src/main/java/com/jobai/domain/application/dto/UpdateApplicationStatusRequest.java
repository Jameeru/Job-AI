package com.jobai.domain.application.dto;

import com.jobai.domain.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request to update an application's status.
 *
 * @param status the new status (required)
 * @param notes  reason or context for the status change (optional)
 */
public record UpdateApplicationStatusRequest(
    @NotNull(message = "status is required")
    ApplicationStatus status,
    String notes
) {}
