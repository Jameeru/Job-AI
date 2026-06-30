package com.jobai.domain.application.dto;

import java.util.Map;

/**
 * Dashboard statistics for the user's applications.
 *
 * @param total        total number of applications tracked
 * @param byStatus     count of applications keyed by status name
 */
public record ApplicationStatsResponse(
    long total,
    Map<String, Long> byStatus
) {}
