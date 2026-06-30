package com.jobai.domain.job.dto;

import com.jobai.domain.job.entity.JobSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * API response DTO for a scraped and AI-analysed job posting.
 */
public record JobResponse(
    UUID id,
    String externalId,
    JobSource source,
    String jobTitle,
    String companyName,
    String location,
    Boolean isRemote,
    String jobDescription,
    String requiredSkills,
    String preferredSkills,
    Short experienceMin,
    Short experienceMax,
    Integer salaryMin,
    Integer salaryMax,
    String salaryCurrency,
    String applicationUrl,
    BigDecimal matchScore,
    Boolean isRejected,
    String rejectionReason,
    String aiAnalysis,
    Instant scrapedAt
) {}
