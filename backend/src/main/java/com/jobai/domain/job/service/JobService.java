package com.jobai.domain.job.service;

import com.jobai.domain.job.dto.JobResponse;
import com.jobai.domain.job.entity.Job;
import com.jobai.domain.job.repository.JobRepository;
import com.jobai.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Business logic for the Job domain.
 *
 * <p>Queries are user-agnostic (jobs are shared across users).
 * Filtering by rejection/score lets each user see the same pool
 * without maintaining per-user job copies.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    /** Return all non-rejected jobs ordered by match score desc. */
    @Transactional(readOnly = true)
    public Page<JobResponse> findQualifiedJobs(Pageable pageable) {
        return jobRepository.findByIsRejectedFalseOrderByMatchScoreDesc(pageable)
                            .map(this::toResponse);
    }

    /** Return all jobs (including rejected), ordered by scraped_at desc. */
    @Transactional(readOnly = true)
    public Page<JobResponse> findAllJobs(Pageable pageable) {
        return jobRepository.findAll(pageable).map(this::toResponse);
    }

    /** Return a single job by id. */
    @Transactional(readOnly = true)
    public JobResponse findById(UUID id) {
        return jobRepository.findById(id)
                            .map(this::toResponse)
                            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
    }

    /**
     * Manually reject a job (e.g., user doesn't want to apply).
     *
     * @param id     job id
     * @param reason human-readable reason (optional)
     */
    @Transactional
    public JobResponse rejectJob(UUID id, String reason) {
        Job job = jobRepository.findById(id)
                               .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
        job.setIsRejected(true);
        job.setRejectionReason(reason);
        log.info("Manually rejected job {} ({})", id, job.getJobTitle());
        return toResponse(jobRepository.save(job));
    }

    // ── Mapping ──────────────────────────────────────────────────

    public JobResponse toResponse(Job job) {
        return new JobResponse(
            job.getId(),
            job.getExternalId(),
            job.getSource(),
            job.getJobTitle(),
            job.getCompanyName(),
            job.getLocation(),
            job.getIsRemote(),
            job.getJobDescription(),
            job.getRequiredSkills(),
            job.getPreferredSkills(),
            job.getExperienceMin(),
            job.getExperienceMax(),
            job.getSalaryMin(),
            job.getSalaryMax(),
            job.getSalaryCurrency(),
            job.getApplicationUrl(),
            job.getMatchScore(),
            job.getIsRejected(),
            job.getRejectionReason(),
            job.getAiAnalysis(),
            job.getScrapedAt()
        );
    }
}
