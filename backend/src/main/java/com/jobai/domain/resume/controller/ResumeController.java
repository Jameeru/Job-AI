package com.jobai.domain.resume.controller;

import com.jobai.domain.resume.dto.ResumeGenerateRequest;
import com.jobai.domain.resume.dto.ResumeResponse;
import com.jobai.domain.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API for AI-generated resume management.
 *
 * <p>The authenticated user's Firebase UID is extracted from the security context
 * (populated by {@code FirebaseAuthFilter}).
 */
@Tag(name = "Resumes", description = "Generate and download AI-tailored resumes")
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(summary = "Generate a new tailored resume for a specific job")
    @PostMapping("/generate")
    public ResponseEntity<ResumeResponse> generate(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ResumeGenerateRequest request
    ) {
        return ResponseEntity.ok(resumeService.generate(userDetails.getUsername(), request));
    }

    @Operation(summary = "List all resume versions for the authenticated user")
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> list(
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(resumeService.listForUser(userDetails.getUsername()));
    }

    @Operation(summary = "Get a specific resume version by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getById(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(resumeService.findById(userDetails.getUsername(), id));
    }

    @Operation(summary = "Download the PDF for a resume version")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
        @AuthenticationPrincipal UserDetails userDetails,
        @PathVariable UUID id
    ) {
        byte[] pdfBytes = resumeService.getPdfBytes(userDetails.getUsername(), id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume_" + id + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }
}
