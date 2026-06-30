package com.jobai.domain.resume.service;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.jobai.domain.job.entity.Job;
import com.jobai.domain.job.repository.JobRepository;
import com.jobai.domain.resume.dto.ResumeGenerateRequest;
import com.jobai.domain.resume.dto.ResumeResponse;
import com.jobai.domain.resume.entity.ResumeVersion;
import com.jobai.domain.resume.repository.ResumeVersionRepository;
import com.jobai.domain.user.entity.UserProfile;
import com.jobai.domain.user.repository.UserProfileRepository;
import com.jobai.domain.user.service.CandidateProfileBuilder;
import com.jobai.infrastructure.ai.BedrockAIClient;
import com.jobai.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Handles AI-driven resume generation and PDF rendering.
 *
 * <p>Flow:
 * <ol>
 *   <li>Load candidate profile + job details</li>
 *   <li>Call {@link BedrockAIClient#generateResume} to get HTML</li>
 *   <li>Render HTML → PDF using iText html2pdf</li>
 *   <li>Persist as a {@link ResumeVersion}</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final BedrockAIClient          aiClient;
    private final JobRepository            jobRepository;
    private final UserProfileRepository    userProfileRepository;
    private final ResumeVersionRepository  resumeVersionRepository;
    private final CandidateProfileBuilder  profileBuilder;

    @Value("${jobai.resume.output-dir}")
    private String resumeOutputDir;

    // ── Generate ──────────────────────────────────────────────────

    /**
     * Generate and persist a tailored resume for the authenticated user + given job.
     *
     * @param firebaseUid authenticated user's Firebase UID
     * @param request     contains the jobId to tailor for
     * @return the saved ResumeVersion as a response DTO
     */
    @Transactional
    public ResumeResponse generate(String firebaseUid, ResumeGenerateRequest request) {
        UserProfile profile = resolveProfile(firebaseUid);
        Job job = resolveJob(request.jobId());

        String candidateProfile = profileBuilder.build(profile);
        String jobAnalysis      = job.getAiAnalysis() != null ? job.getAiAnalysis() : "{}";
        String jobDescription   = buildJobDescription(job);

        log.info("Generating resume for user {} — job: {} @ {}", firebaseUid, job.getJobTitle(), job.getCompanyName());

        String htmlContent = aiClient.generateResume(candidateProfile, jobAnalysis, jobDescription);

        // Render PDF
        String pdfPath = renderPdf(htmlContent, profile.getId(), job.getId());

        // Build label
        String label = job.getJobTitle() + " @ " + job.getCompanyName()
                       + " v" + (resumeVersionRepository.countByUserId(profile.getId()) + 1);

        ResumeVersion version = ResumeVersion.builder()
            .userId(profile.getId())
            .jobId(job.getId())
            .versionLabel(label)
            .htmlContent(htmlContent)
            .pdfPath(pdfPath)
            .generatedAt(Instant.now())
            .build();

        return toResponse(resumeVersionRepository.save(version));
    }

    // ── List ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ResumeResponse> listForUser(String firebaseUid) {
        UserProfile profile = resolveProfile(firebaseUid);
        return resumeVersionRepository.findByUserIdOrderByGeneratedAtDesc(profile.getId())
                                      .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ResumeResponse findById(String firebaseUid, UUID resumeId) {
        UserProfile profile = resolveProfile(firebaseUid);
        return resumeVersionRepository.findByIdAndUserId(resumeId, profile.getId())
            .map(this::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + resumeId));
    }

    /** Returns the raw PDF bytes for download. */
    @Transactional(readOnly = true)
    public byte[] getPdfBytes(String firebaseUid, UUID resumeId) {
        UserProfile profile = resolveProfile(firebaseUid);
        ResumeVersion version = resumeVersionRepository.findByIdAndUserId(resumeId, profile.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + resumeId));

        if (version.getPdfPath() == null) {
            throw new ResourceNotFoundException("PDF not yet generated for resume: " + resumeId);
        }

        try {
            return Files.readAllBytes(Paths.get(version.getPdfPath()));
        } catch (Exception e) {
            log.error("Failed to read PDF file {}: {}", version.getPdfPath(), e.getMessage());
            throw new RuntimeException("PDF file not accessible", e);
        }
    }

    // ── Private helpers ───────────────────────────────────────────

    private String renderPdf(String htmlContent, UUID userId, UUID jobId) {
        try {
            Path outDir = Paths.get(resumeOutputDir);
            Files.createDirectories(outDir);

            String fileName = "resume_" + userId + "_" + jobId + "_" + System.currentTimeMillis() + ".pdf";
            Path pdfPath = outDir.resolve(fileName);

            try (OutputStream os = new FileOutputStream(pdfPath.toFile())) {
                ConverterProperties props = new ConverterProperties();
                HtmlConverter.convertToPdf(htmlContent, os, props);
            }

            log.info("PDF rendered: {}", pdfPath);
            return pdfPath.toString();
        } catch (Exception e) {
            log.error("PDF rendering failed: {}", e.getMessage(), e);
            return null; // Non-fatal — HTML is still saved
        }
    }

    private String buildJobDescription(Job job) {
        return String.format(
            "Title: %s\nCompany: %s\nLocation: %s\nRequired Skills: %s\nPreferred Skills: %s",
            job.getJobTitle(),
            job.getCompanyName(),
            job.getLocation() != null ? job.getLocation() : "Not specified",
            job.getRequiredSkills() != null ? job.getRequiredSkills() : "Not specified",
            job.getPreferredSkills() != null ? job.getPreferredSkills() : "None"
        );
    }

    private UserProfile resolveProfile(String firebaseUid) {
        return userProfileRepository.findByFirebaseUidWithDetails(firebaseUid)
            .orElseThrow(() -> new ResourceNotFoundException("User profile not found. Create one at POST /api/profile"));
    }

    private Job resolveJob(UUID jobId) {
        return jobRepository.findById(jobId)
            .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + jobId));
    }

    private ResumeResponse toResponse(ResumeVersion v) {
        return new ResumeResponse(
            v.getId(),
            v.getJobId(),
            v.getVersionLabel(),
            v.getHtmlContent(),
            v.getPdfPath(),
            v.getKeywordsUsed(),
            v.getAtsScore(),
            v.getGeneratedAt()
        );
    }
}
