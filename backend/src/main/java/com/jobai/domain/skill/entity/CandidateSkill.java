package com.jobai.domain.skill.entity;

import com.jobai.domain.user.entity.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A technical skill owned by a candidate.
 *
 * <p>Category examples: BACKEND, FRONTEND, DATABASE, TOOL, CLOUD
 * Proficiency examples: BEGINNER, INTERMEDIATE, ADVANCED
 */
@Entity
@Table(name = "candidate_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile userProfile;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "proficiency", length = 20)
    private String proficiency;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
