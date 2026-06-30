package com.jobai.domain.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Sub-DTO for project data inside profile requests.
 */
@Data
@Builder
public class ProjectRequest {
    private String projectName;
    private String description;
    private String techStack;       // comma-separated: "Java, Spring Boot, MySQL"
    private String githubUrl;
    private String liveUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isFeatured;
}
