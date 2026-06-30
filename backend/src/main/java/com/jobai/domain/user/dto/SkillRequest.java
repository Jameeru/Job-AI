package com.jobai.domain.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Sub-DTO for skill data inside profile requests.
 */
@Data
@Builder
public class SkillRequest {
    private String skillName;
    private String category;    // BACKEND, FRONTEND, DATABASE, TOOL, CLOUD
    private String proficiency; // BEGINNER, INTERMEDIATE, ADVANCED
}
