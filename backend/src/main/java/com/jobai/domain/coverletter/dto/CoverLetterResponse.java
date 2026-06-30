package com.jobai.domain.coverletter.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * API response for a generated cover letter.
 *
 * @param id          unique cover letter ID
 * @param jobId       the job this was written for
 * @param content     full text of the cover letter
 * @param pdfPath     path to the PDF version (nullable)
 * @param generatedAt generation timestamp
 */
public record CoverLetterResponse(
    UUID id,
    UUID jobId,
    String content,
    String pdfPath,
    Instant generatedAt
) {}
