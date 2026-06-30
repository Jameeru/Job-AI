package com.jobai.domain.coverletter.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * An AI-generated cover letter tailored for a specific job.
 *
 * <p>Cover letters are unique per job and always truthful —
 * the AI is instructed never to fabricate qualifications.
 *
 * <p>Uses plain UUID foreign keys to avoid JPA lazy-loading complexity.
 */
@Entity
@Table(name = "cover_letters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoverLetter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "pdf_path", length = 500)
    private String pdfPath;

    @Column(name = "generated_at", nullable = false)
    @Builder.Default
    private Instant generatedAt = Instant.now();
}
