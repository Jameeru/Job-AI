package com.jobai.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobai.domain.job.entity.Job;
import com.jobai.domain.job.entity.JobSource;
import com.jobai.domain.job.repository.JobRepository;
import com.jobai.domain.user.entity.UserProfile;
import com.jobai.domain.user.repository.UserProfileRepository;
import com.jobai.domain.user.service.CandidateProfileBuilder;
import com.jobai.infrastructure.ai.BedrockAIClient;
import com.jobai.infrastructure.scraper.JobScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Core orchestration service that drives the daily job search pipeline.
 *
 * <p>Pipeline steps (run on schedule):
 * <ol>
 *   <li>Invoke all registered {@link JobScraper} beans to collect raw jobs</li>
 *   <li>De-duplicate against existing DB records (by externalId + source)</li>
 *   <li>For each new job, call {@link BedrockAIClient#analyzeJob} to get a match score</li>
 *   <li>Persist jobs — mark those scoring below threshold as rejected</li>
 * </ol>
 *
 * <p>The candidate profile used for AI analysis is built from the first active
 * user found in the system. In a multi-tenant setup this would run per-user,
 * but for a single-user fresher tool this is acceptable.
 */
@Slf4j
@Service
@EnableScheduling
@RequiredArgsConstructor
public class JobSearchOrchestrator {

    private final List<JobScraper>          scrapers;
    private final JobRepository             jobRepository;
    private final UserProfileRepository     userProfileRepository;
    private final BedrockAIClient           aiClient;
    private final CandidateProfileBuilder   profileBuilder;
    private final ObjectMapper              objectMapper;

    @Value("${jobai.scraper.min-match-score}")
    private double minMatchScore;

    @Value("${jobai.scheduler.enabled}")
    private boolean schedulerEnabled;

    // ── Scheduled entry point ─────────────────────────────────────

    /**
     * Runs daily at 7 AM IST (configurable via {@code JOB_SEARCH_CRON}).
     * Can be triggered manually via {@link #runNow()}.
     */
    @Scheduled(cron = "${jobai.scheduler.job-search-cron}")
    public void scheduledRun() {
        if (!schedulerEnabled) {
            log.info("Job scheduler is disabled — skipping run");
            return;
        }
        log.info("=== Scheduled job search run started ===");
        runNow();
        log.info("=== Scheduled job search run complete ===");
    }

    /**
     * Manually triggers the full pipeline immediately.
     * Callable from tests or a future admin endpoint.
     */
    @Transactional
    public void runNow() {
        String candidateProfile = resolveCandidateProfile();
        if (candidateProfile == null) {
            log.warn("No active user profile found — job analysis skipped. " +
                     "Create a user profile at POST /api/profile first.");
            return;
        }

        int totalScraped  = 0;
        int totalNew      = 0;
        int totalAccepted = 0;
        int totalRejected = 0;

        List<Job> allScrapedJobs = new ArrayList<>();

        for (JobScraper scraper : scrapers) {
            JobSource source = scraper.getSource();
            log.info("Running scraper: {}", source);

            List<Job> scraped;
            try {
                scraped = scraper.scrape();
                if (scraped != null) {
                    allScrapedJobs.addAll(scraped);
                }
            } catch (Exception e) {
                log.error("Scraper {} failed: {}", source, e.getMessage(), e);
            }
        }

        totalScraped = allScrapedJobs.size();

        for (Job job : allScrapedJobs) {
            if (isDuplicate(job)) continue;

            totalNew++;
            Job analysed = analyseAndEnrich(job, candidateProfile);

            if (analysed.getMatchScore() != null &&
                analysed.getMatchScore().doubleValue() >= minMatchScore) {
                totalAccepted++;
            } else {
                totalRejected++;
            }

            try {
                jobRepository.save(analysed);
            } catch (Exception e) {
                log.warn("Failed to save job [{} @ {}]: {}", job.getJobTitle(), job.getSource(), e.getMessage());
            }
        }

        log.info("Pipeline summary — scraped: {}, new: {}, accepted: {}, auto-rejected: {}",
                 totalScraped, totalNew, totalAccepted, totalRejected);
    }

    // ── Private helpers ───────────────────────────────────────────

    private boolean isDuplicate(Job job) {
        if (job.getExternalId() == null) return false;
        return jobRepository.existsByExternalIdAndSource(job.getExternalId(), job.getSource());
    }

    /**
     * Calls Claude to analyse the job against the candidate profile.
     * Enriches the job entity in-place with score, analysis JSON, and rejection flag.
     */
    private Job analyseAndEnrich(Job job, String candidateProfile) {
        try {
            String description = buildDescription(job);
            String analysisJson = aiClient.analyzeJob(description, candidateProfile);
            job.setAiAnalysis(analysisJson);

            // Parse match score from JSON
            Map<?, ?> analysis = objectMapper.readValue(analysisJson, Map.class);
            Object scoreRaw = analysis.get("matchScore");
            if (scoreRaw != null) {
                BigDecimal score = new BigDecimal(scoreRaw.toString());
                job.setMatchScore(score);

                if (score.doubleValue() < minMatchScore) {
                    job.setIsRejected(true);
                    Object reason = analysis.get("rejectionReason");
                    job.setRejectionReason(reason != null ? reason.toString() : "Score below threshold");
                    log.debug("Auto-rejected: {} @ {} (score: {})", job.getJobTitle(), job.getCompanyName(), score);
                } else {
                    log.info("Accepted: {} @ {} (score: {})", job.getJobTitle(), job.getCompanyName(), score);
                }
            }

            // Enrich missing fields from AI analysis
            enrichFromAnalysis(job, analysis);

        } catch (Exception e) {
            log.warn("AI analysis failed for [{} @ {}]: {}", job.getJobTitle(), job.getCompanyName(), e.getMessage());
        }
        return job;
    }

    private void enrichFromAnalysis(Job job, Map<?, ?> analysis) {
        if (job.getJobDescription() == null || job.getJobDescription().isBlank()) {
            // Already in DB as scraped text
        }
        Object isRemote = analysis.get("isRemote");
        if (isRemote instanceof Boolean) job.setIsRemote((Boolean) isRemote);

        // Merge required skills if empty
        if ((job.getRequiredSkills() == null || job.getRequiredSkills().isBlank())) {
            Object skills = analysis.get("requiredSkills");
            if (skills instanceof List<?> list) {
                job.setRequiredSkills(String.join(", ", list.stream().map(Object::toString).toList()));
            }
        }
    }

    private String buildDescription(Job job) {
        return String.format(
            "Job Title: %s\nCompany: %s\nLocation: %s\nRequired Skills: %s",
            job.getJobTitle(),
            job.getCompanyName(),
            job.getLocation() != null ? job.getLocation() : "Not specified",
            job.getRequiredSkills() != null ? job.getRequiredSkills() : "Not specified"
        );
    }

    private String resolveCandidateProfile() {
        return userProfileRepository.findFirstActiveWithDetails()
            .map(profileBuilder::build)
            .orElse(null);
    }
}
