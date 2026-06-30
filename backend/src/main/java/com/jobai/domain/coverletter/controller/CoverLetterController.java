package com.jobai.domain.coverletter.controller;

import com.jobai.domain.coverletter.dto.CoverLetterGenerateRequest;
import com.jobai.domain.coverletter.dto.CoverLetterResponse;
import com.jobai.domain.coverletter.service.CoverLetterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST API for AI-generated cover letter management.
 */
@Tag(name = "Cover Letters", description = "Generate and manage AI-written cover letters")
@RestController
@RequestMapping("/api/cover-letters")
@RequiredArgsConstructor
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    @Operation(summary = "Generate a new cover letter for a specific job")
    @PostMapping("/generate")
    public ResponseEntity<CoverLetterResponse> generate(
        @AuthenticationPrincipal FirebaseToken token,
        @Valid @RequestBody CoverLetterGenerateRequest request
    ) {
        return ResponseEntity.ok(coverLetterService.generate(token.getUid(), request));
    }

    @Operation(summary = "List all cover letters for the authenticated user")
    @GetMapping
    public ResponseEntity<List<CoverLetterResponse>> list(
        @AuthenticationPrincipal FirebaseToken token
    ) {
        return ResponseEntity.ok(coverLetterService.listForUser(token.getUid()));
    }

    @Operation(summary = "Get a specific cover letter by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CoverLetterResponse> getById(
        @AuthenticationPrincipal FirebaseToken token,
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(coverLetterService.findById(token.getUid(), id));
    }
}
