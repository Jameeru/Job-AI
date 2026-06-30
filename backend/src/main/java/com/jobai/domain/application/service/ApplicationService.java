package com.jobai.domain.application.service;

import com.jobai.domain.application.dto.*;
import com.jobai.domain.application.entity.Application;
import com.jobai.domain.application.entity.ApplicationStatus;
import com.jobai.domain.application.entity.ApplicationStatusHistory;
import com.jobai.domain.application.repository.ApplicationRepository;
import com.jobai.domain.application.repository.ApplicationStatusHistoryRepository;
import com.jobai.domain.job.repository.JobRepository;
import com.jobai.domain.user.entity.UserProfile;
import com.jobai.domain.user.repository.UserProfileRepository;
import com.jobai.shared.exception.ConflictException;
import com.jobai.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Business logic for tracking job applications.
 *
 * <p>Every status change is recorded in {@link ApplicationStatusHistory}
 * to provide a full audit trail and timeline for the candidate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository            applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final UserProfileRepository            userProfileRepository;
    private final JobRepository                   jobRepository;

    // ── Create ────────────────────────────────────────────────────

    @Transactional
    public ApplicationResponse create(String firebaseUid, CreateApplicationRequest request) {
        UserProfile profile = resolveProfile(firebaseUid);

        if (!jobRepository.existsById(request.jobId())) {
            throw new ResourceNotFoundException("Job not found: " + request.jobId());
        }

        if (applicationRepository.existsByUserIdAndJobId(profile.getId(), request.jobId())) {
            throw new ConflictException("You have already tracked an application for this job.");
        }

        Application application = Application.builder()
            .userId(profile.getId())
            .jobId(request.jobId())
            .resumeVersionId(request.resumeVersionId())
            .coverLetterId(request.coverLetterId())
            .notes(request.notes())
            .status(ApplicationStatus.PENDING)
            .build();

        Application saved = applicationRepository.save(application);

        // Record initial status in history
        recordHistory(saved.getId(), null, ApplicationStatus.PENDING, "Application created");

        log.info("Application created: user={} job={}", profile.getId(), request.jobId());
        return toResponse(saved);
    }

    // ── List ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listForUser(String firebaseUid, ApplicationStatus statusFilter) {
        UserProfile profile = resolveProfile(firebaseUid);
        List<Application> apps = statusFilter != null
            ? applicationRepository.findByUserIdAndStatusOrderByCreatedAtDesc(profile.getId(), statusFilter)
            : applicationRepository.findByUserIdOrderByCreatedAtDesc(profile.getId());
        return apps.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(String firebaseUid, UUID appId) {
        UserProfile profile = resolveProfile(firebaseUid);
        return applicationRepository.findByIdAndUserId(appId, profile.getId())
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + appId));
    }

    // ── Update Status ─────────────────────────────────────────────

    @Transactional
    public ApplicationResponse updateStatus(String firebaseUid, UUID appId, UpdateApplicationStatusRequest request) {
        UserProfile profile = resolveProfile(firebaseUid);
        Application app = applicationRepository.findByIdAndUserId(appId, profile.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + appId));

        ApplicationStatus previousStatus = app.getStatus();
        app.setStatus(request.status());

        // Set appliedAt timestamp when moving to APPLIED
        if (request.status() == ApplicationStatus.APPLIED && app.getAppliedAt() == null) {
            app.setAppliedAt(Instant.now());
        }

        Application saved = applicationRepository.save(app);
        recordHistory(saved.getId(), previousStatus, request.status(), request.notes());

        log.info("Application {} status: {} -> {}", appId, previousStatus, request.status());
        return toResponse(saved);
    }

    // ── Stats ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ApplicationStatsResponse getStats(String firebaseUid) {
        UserProfile profile = resolveProfile(firebaseUid);
        long total = applicationRepository.countByUserId(profile.getId());

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ApplicationStatus s : ApplicationStatus.values()) {
            long count = applicationRepository.countByUserIdAndStatus(profile.getId(), s);
            if (count > 0) byStatus.put(s.name(), count);
        }

        return new ApplicationStatsResponse(total, byStatus);
    }

    // ── Private helpers ───────────────────────────────────────────

    private void recordHistory(UUID applicationId, ApplicationStatus from, ApplicationStatus to, String notes) {
        historyRepository.save(ApplicationStatusHistory.builder()
            .applicationId(applicationId)
            .fromStatus(from)
            .toStatus(to)
            .changedAt(Instant.now())
            .notes(notes)
            .build());
    }

    private UserProfile resolveProfile(String firebaseUid) {
        return userProfileRepository.findByFirebaseUid(firebaseUid)
            .orElseThrow(() -> new ResourceNotFoundException(
                "User profile not found. Create one at POST /api/profile"));
    }

    private ApplicationResponse toResponse(Application a) {
        return new ApplicationResponse(
            a.getId(),
            a.getJobId(),
            a.getResumeVersionId(),
            a.getCoverLetterId(),
            a.getStatus(),
            a.getAppliedAt(),
            a.getInterviewDate(),
            a.getOfferAmount(),
            a.getNotes(),
            a.getCreatedAt(),
            a.getUpdatedAt()
        );
    }
}
