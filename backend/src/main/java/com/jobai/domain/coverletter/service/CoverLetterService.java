package com.jobai.domain.coverletter.service;

import com.jobai.domain.coverletter.dto.CoverLetterGenerateRequest;
import com.jobai.domain.coverletter.dto.CoverLetterResponse;
import com.jobai.domain.coverletter.entity.CoverLetter;
import com.jobai.domain.coverletter.repository.CoverLetterRepository;
import com.jobai.domain.job.entity.Job;
import com.jobai.domain.job.repository.JobRepository;
import com.jobai.domain.user.entity.UserProfile;
import com.jobai.domain.user.repository.UserProfileRepository;
import com.jobai.domain.user.service.CandidateProfileBuilder;
import com.jobai.infrastructure.ai.BedrockAIClient;
import com.jobai.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Generates and persists AI-written cover letters.
 *
 * <p>If a cover letter already exists for this user + job combination,
 * it is overwritten (regenerated) to ensure freshness.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoverLetterService {

    private final BedrockAIClient         aiClient;
    private final JobRepository           jobRepository;
    private final UserProfileRepository   userProfileRepository;
    private final CoverLetterRepository   coverLetterRepository;
    private final CandidateProfileBuilder profileBuilder;

    // ── Generate ──────────────────────────────────────────────────

    @Transactional
    public CoverLetterResponse generate(String firebaseUid, CoverLetterGenerateRequest request) {
        UserProfile profile = resolveProfile(firebaseUid);
        Job job = resolveJob(request.jobId());

        String candidateProfile = profileBuilder.build(profile);
        String jobAnalysis      = job.getAiAnalysis() != null ? job.getAiAnalysis() : "{}";

        log.info("Generating cover letter for user {} — job: {} @ {}",
                 firebaseUid, job.getJobTitle(), job.getCompanyName());

        String letterContent = aiClient.generateCoverLetter(
            candidateProfile, jobAnalysis, job.getCompanyName(), job.getJobTitle()
        );

        // Overwrite if one already exists for this job
        CoverLetter letter = coverLetterRepository
            .findByUserIdAndJobId(profile.getId(), job.getId())
            .orElse(CoverLetter.builder()
                .userId(profile.getId())
                .jobId(job.getId())
                .build());

        letter.setContent(letterContent);
        letter.setGeneratedAt(Instant.now());

        return toResponse(coverLetterRepository.save(letter));
    }

    // ── List / Get ────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CoverLetterResponse> listForUser(String firebaseUid) {
        UserProfile profile = resolveProfile(firebaseUid);
        return coverLetterRepository
            .findByUserIdOrderByGeneratedAtDesc(profile.getId())
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CoverLetterResponse findById(String firebaseUid, UUID coverId) {
        UserProfile profile = resolveProfile(firebaseUid);
        return coverLetterRepository
            .findByIdAndUserId(coverId, profile.getId())
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Cover letter not found: " + coverId));
    }

    // ── Private helpers ───────────────────────────────────────────

    private UserProfile resolveProfile(String firebaseUid) {
        return userProfileRepository.findByFirebaseUidWithDetails(firebaseUid)
            .orElseThrow(() -> new ResourceNotFoundException(
                "User profile not found. Create one at POST /api/profile"));
    }

    private Job resolveJob(UUID jobId) {
        return jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
    }

    private CoverLetterResponse toResponse(CoverLetter cl) {
        return new CoverLetterResponse(
            cl.getId(),
            cl.getJobId(),
            cl.getContent(),
            cl.getPdfPath(),
            cl.getGeneratedAt()
        );
    }
}
