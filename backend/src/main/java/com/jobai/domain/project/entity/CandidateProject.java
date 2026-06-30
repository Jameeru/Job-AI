package com.jobai.domain.project.entity;

import com.jobai.domain.user.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * An academic or personal project owned by a candidate.
 *
 * <p>Used by the resume builder to highlight relevant projects per job posting.
 * Only real, verifiable projects should be stored here.
 */
@Entity
@Table(name = "candidate_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProject {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @Column(name = "project_name", nullable = false, length = 255)
    private String projectName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Comma-separated list of technologies used. */
    @Column(name = "tech_stack", length = 500)
    private String techStack;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "live_url", length = 500)
    private String liveUrl;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
