package com.jobai.domain.application.entity;

/**
 * Lifecycle status of a job application.
 */
public enum ApplicationStatus {
    PENDING,
    APPLIED,
    UNDER_REVIEW,
    INTERVIEW_SCHEDULED,
    INTERVIEWED,
    OFFER_RECEIVED,
    REJECTED,
    WITHDRAWN
}
