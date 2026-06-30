package com.jobai.domain.job.controller;

import com.jobai.domain.job.dto.JobResponse;
import com.jobai.domain.job.service.JobService;
import com.google.firebase.auth.FirebaseToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST API for browsing and managing scraped job postings.
 *
 * <p>All endpoints require a valid Firebase ID token (enforced by FirebaseAuthFilter).
 * Jobs are shared across all users — filtering is done server-side by score/rejection.
 */
@Tag(name = "Jobs", description = "Browse and manage AI-analysed job postings")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @Operation(summary = "List qualified jobs (score >= 8, not rejected), newest first")
    @GetMapping
    public ResponseEntity<Page<JobResponse>> listQualifiedJobs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "matchScore"));
        return ResponseEntity.ok(jobService.findQualifiedJobs(pageable));
    }

    @Operation(summary = "List ALL jobs including rejected")
    @GetMapping("/all")
    public ResponseEntity<Page<JobResponse>> listAllJobs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "scrapedAt"));
        return ResponseEntity.ok(jobService.findAllJobs(pageable));
    }

    @Operation(summary = "Get a single job by ID")
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.findById(id));
    }

    @Operation(summary = "Manually reject a job")
    @PostMapping("/{id}/reject")
    public ResponseEntity<JobResponse> rejectJob(
        @PathVariable UUID id,
        @RequestBody(required = false) Map<String, String> body
    ) {
        String reason = body != null ? body.getOrDefault("reason", "Manually rejected") : "Manually rejected";
        return ResponseEntity.ok(jobService.rejectJob(id, reason));
    }
}

