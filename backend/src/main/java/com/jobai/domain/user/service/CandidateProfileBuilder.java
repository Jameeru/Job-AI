package com.jobai.domain.user.service;

import com.jobai.domain.certification.entity.CandidateCertification;
import com.jobai.domain.project.entity.CandidateProject;
import com.jobai.domain.skill.entity.CandidateSkill;
import com.jobai.domain.user.entity.UserProfile;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Builds a structured plain-text summary of a candidate's profile.
 *
 * <p>This text is injected into every AI prompt as context.
 * Keeping it well-structured and factual is critical — Claude uses it
 * verbatim to avoid hallucinating qualifications the candidate doesn't have.
 */
@Component
public class CandidateProfileBuilder {

    /**
     * Produces a concise, AI-friendly profile string from the given UserProfile.
     *
     * @param profile a fully-loaded UserProfile (with skills, projects, certs)
     * @return formatted plain-text summary
     */
    public String build(UserProfile profile) {
        StringBuilder sb = new StringBuilder();

        // ── Personal ──────────────────────────────────────────────
        sb.append("NAME: ").append(nullSafe(profile.getFullName())).append("\n");
        sb.append("EMAIL: ").append(nullSafe(profile.getEmail())).append("\n");

        if (profile.getPhone() != null)
            sb.append("PHONE: ").append(profile.getPhone()).append("\n");
        if (profile.getCity() != null)
            sb.append("CITY: ").append(profile.getCity()).append("\n");
        if (profile.getLinkedinUrl() != null)
            sb.append("LINKEDIN: ").append(profile.getLinkedinUrl()).append("\n");
        if (profile.getGithubUrl() != null)
            sb.append("GITHUB: ").append(profile.getGithubUrl()).append("\n");
        if (profile.getPortfolioUrl() != null)
            sb.append("PORTFOLIO: ").append(profile.getPortfolioUrl()).append("\n");

        // ── Education ─────────────────────────────────────────────
        sb.append("\nEDUCATION:\n");
        sb.append("  Degree: ").append(nullSafe(profile.getDegree()))
          .append(" in ").append(nullSafe(profile.getBranch())).append("\n");
        sb.append("  College: ").append(nullSafe(profile.getCollegeName())).append("\n");
        if (profile.getGraduationYear() != null)
            sb.append("  Graduation Year: ").append(profile.getGraduationYear()).append("\n");
        if (profile.getCgpa() != null)
            sb.append("  CGPA: ").append(profile.getCgpa()).append("\n");

        // ── Skills ────────────────────────────────────────────────
        if (profile.getSkills() != null && !profile.getSkills().isEmpty()) {
            sb.append("\nSKILLS:\n");
            profile.getSkills().stream()
                .collect(Collectors.groupingBy(
                    s -> nullSafe(s.getCategory()),
                    Collectors.toList()
                ))
                .forEach((category, skills) -> {
                    sb.append("  ").append(category).append(": ");
                    sb.append(skills.stream()
                        .map(CandidateSkill::getSkillName)
                        .collect(Collectors.joining(", ")));
                    sb.append("\n");
                });
        }

        // ── Projects ──────────────────────────────────────────────
        if (profile.getProjects() != null && !profile.getProjects().isEmpty()) {
            sb.append("\nPROJECTS:\n");
            for (CandidateProject p : profile.getProjects()) {
                sb.append("  - ").append(p.getProjectName()).append("\n");
                if (p.getDescription() != null)
                    sb.append("    Description: ").append(p.getDescription()).append("\n");
                if (p.getTechStack() != null)
                    sb.append("    Tech Stack: ").append(p.getTechStack()).append("\n");
                if (p.getGithubUrl() != null)
                    sb.append("    GitHub: ").append(p.getGithubUrl()).append("\n");
                if (p.getLiveUrl() != null)
                    sb.append("    Live: ").append(p.getLiveUrl()).append("\n");
            }
        }

        // ── Certifications ────────────────────────────────────────
        if (profile.getCertifications() != null && !profile.getCertifications().isEmpty()) {
            sb.append("\nCERTIFICATIONS:\n");
            for (CandidateCertification c : profile.getCertifications()) {
                sb.append("  - ").append(c.getCertName());
                if (c.getIssuer() != null) sb.append(" by ").append(c.getIssuer());
                if (c.getCredentialUrl() != null) sb.append(" [").append(c.getCredentialUrl()).append("]");
                sb.append("\n");
            }
        }

        // ── Salary expectations ───────────────────────────────────
        if (profile.getExpectedSalaryMin() != null) {
            sb.append("\nEXPECTED SALARY: Rs.")
              .append(profile.getExpectedSalaryMin() / 100000).append("L");
            if (profile.getExpectedSalaryMax() != null)
                sb.append(" - Rs.").append(profile.getExpectedSalaryMax() / 100000).append("L");
            sb.append(" per annum\n");
        }

        return sb.toString();
    }

    private String nullSafe(String value) {
        return value != null ? value : "N/A";
    }
}
