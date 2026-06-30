package com.jobai.domain.resume.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * API response for a generated resume version.
 *
 * @param id           unique resume version ID
 * @param jobId        the job this resume was tailored for (nullable)
 * @param versionLabel human-readable label (e.g. "Java Full Stack v3")
 * @param htmlContent  full HTML of the resume (for preview/editing)
 * @param pdfPath      relative path to the generated PDF file (nullable if not yet rendered)
 * @param keywordsUsed ATS keywords injected into the resume
 * @param atsScore     estimated ATS compatibility score (0-10)
 * @param generatedAt  timestamp of generation
 */
public record ResumeResponse(
    UUID id,
    UUID jobId,
    String versionLabel,
    String htmlContent,
    String pdfPath,
    String keywordsUsed,
    java.math.BigDecimal atsScore,
    Instant generatedAt
) {}
