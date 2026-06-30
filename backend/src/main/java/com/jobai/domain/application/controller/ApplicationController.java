package com.jobai.domain.application.controller;

import com.jobai.domain.application.dto.*;
import com.jobai.domain.application.entity.ApplicationStatus;
import com.jobai.domain.application.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API for tracking job applications and their lifecycle.
 *
 * <p>Every status change is audited via {@code application_status_history}.
 */
@Tag(name = "Applications", description = "Track and manage job application lifecycle")
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "Start tracking a new job application")
    @PostMapping
    public ResponseEntity<ApplicationResponse> create(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody CreateApplicationRequest request
    ) {
        return ResponseEntity.ok(applicationService.create(userDetails.getUsername(), request));
    }

    @Operation(summary = "List all applications (optionally filtered by status)")
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> list(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(required = false) ApplicationStatus status
    ) {
        return ResponseEntity.ok(applicationService.listForUser(userDetails.getUsername(), status));
    }

    @Operation(summary = "Get a single application by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getById(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(applicationService.findById(userDetails.getUsername(), id));
    }

    @Operation(summary = "Update the status of an application (e.g., APPLIED → INTERVIEW_SCHEDULED)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateApplicationStatusRequest request
    ) {
        return ResponseEntity.ok(applicationService.updateStatus(userDetails.getUsername(), id, request));
    }

    @Operation(summary = "Get application dashboard statistics (counts by status)")
    @GetMapping("/stats")
    public ResponseEntity<ApplicationStatsResponse> getStats(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(applicationService.getStats(userDetails.getUsername()));
    }
}
