package com.jobai.domain.user.entity;

import com.jobai.domain.skill.entity.CandidateSkill;
import com.jobai.domain.project.entity.CandidateProject;
import com.jobai.domain.certification.entity.CandidateCertification;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the authenticated candidate's persistent profile.
 *
 * <p>The {@code firebaseUid} is the canonical identity — it maps 1:1 with a
 * Firebase user record. On first login, a UserProfile is created automatically.
 *
 * <p>Skill, Project, and Certification data are owned here and cascade on delete.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "firebase_uid", nullable = false, unique = true, length = 128)
    private String firebaseUid;

    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    @Column(name = "college_name", length = 255)
    private String collegeName;

    @Column(name = "degree", length = 100)
    private String degree;

    @Column(name = "branch", length = 100)
    private String branch;

    @Column(name = "graduation_year")
    private Short graduationYear;

    @Column(name = "cgpa", precision = 4, scale = 2)
    private BigDecimal cgpa;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "expected_salary_min")
    private Integer expectedSalaryMin;

    @Column(name = "expected_salary_max")
    private Integer expectedSalaryMax;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    // ── Relationships ─────────────────────────────────────────────

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CandidateSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CandidateProject> projects = new ArrayList<>();

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CandidateCertification> certifications = new ArrayList<>();

    // ── Lifecycle ─────────────────────────────────────────────────

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    // ── Domain helpers ─────────────────────────────────────────────

    /** Add a skill and maintain bidirectional relationship. */
    public void addSkill(CandidateSkill skill) {
        skill.setUserProfile(this);
        this.skills.add(skill);
    }

    /** Add a project and maintain bidirectional relationship. */
    public void addProject(CandidateProject project) {
        project.setUserProfile(this);
        this.projects.add(project);
    }

    /** Add a certification and maintain bidirectional relationship. */
    public void addCertification(CandidateCertification certification) {
        certification.setUserProfile(this);
        this.certifications.add(certification);
    }
}
