package com.jobai.domain.user.controller;

import com.google.firebase.auth.FirebaseToken;
import com.jobai.domain.user.dto.CreateUserProfileRequest;
import com.jobai.domain.user.dto.UpdateUserProfileRequest;
import com.jobai.domain.user.dto.UserProfileResponse;
import com.jobai.domain.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.jobai.domain.resume.service.PdfExtractionService;
import com.jobai.infrastructure.ai.BedrockAIClient;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * REST controller for candidate profile management.
 *
 * <p>All endpoints require a valid Firebase Bearer token.
 * The {@code @AuthenticationPrincipal FirebaseToken} is automatically injected
 * from the Security context populated by {@link com.jobai.config.FirebaseAuthFilter}.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST   /api/profile         — Create profile (first login)</li>
 *   <li>GET    /api/profile         — Get current user's profile</li>
 *   <li>PUT    /api/profile         — Update profile (full or partial)</li>
 *   <li>DELETE /api/profile         — Deactivate profile</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Candidate profile management")
@SecurityRequirement(name = "Bearer Authentication")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final PdfExtractionService pdfExtractionService;
    private final BedrockAIClient bedrockAIClient;
    private final ObjectMapper objectMapper;

    /**
     * POST /api/profile
     * Creates the candidate profile on first login after Firebase authentication.
     */
    @PostMapping
    @Operation(summary = "Create candidate profile", description = "Called once after first Firebase login")
    public ResponseEntity<UserProfileResponse> createProfile(
        @AuthenticationPrincipal FirebaseToken token,
        @Valid @RequestBody CreateUserProfileRequest request
    ) {
        UserProfileResponse response = userProfileService.createProfile(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/profile
     * Returns the authenticated user's full profile including skills, projects, certifications.
     */
    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileResponse> getProfile(
        @AuthenticationPrincipal FirebaseToken token
    ) {
        return ResponseEntity.ok(userProfileService.getProfile(token));
    }

    /**
     * PUT /api/profile
     * Updates the profile. Null fields in the request body are ignored (PATCH semantics).
     */
    @PutMapping
    @Operation(summary = "Update candidate profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
        @AuthenticationPrincipal FirebaseToken token,
        @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return ResponseEntity.ok(userProfileService.updateProfile(token, request));
    }

    /**
     * DELETE /api/profile
     * Soft-deletes the profile. Data is retained for audit purposes.
     */
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deactivate profile (soft delete)")
    public void deactivateProfile(@AuthenticationPrincipal FirebaseToken token) {
        userProfileService.deactivateProfile(token);
    }

    /**
     * POST /api/profile/resume
     * Uploads a resume PDF, extracts text, uses AI to parse it into structured data,
     * and updates the user's profile automatically.
     */
    @PostMapping(value = "/resume", consumes = "multipart/form-data")
    @Operation(summary = "Upload resume to extract data", description = "Parses PDF and uses AI to update profile fields")
    public ResponseEntity<UserProfileResponse> uploadResumeToExtract(
        @AuthenticationPrincipal FirebaseToken token,
        @RequestPart("resume") MultipartFile resumeFile
    ) {
        try {
            // 1. Extract raw text from PDF
            String resumeText = pdfExtractionService.extractTextFromPdf(resumeFile);
            
            // 2. Send to AI to get JSON representing UpdateUserProfileRequest
            String jsonProfile = bedrockAIClient.extractProfileFromResume(resumeText);
            
            // Clean up any markdown code blocks returned by Claude
            if (jsonProfile != null) {
                jsonProfile = jsonProfile.trim();
                if (jsonProfile.startsWith("```json")) {
                    jsonProfile = jsonProfile.substring(7);
                } else if (jsonProfile.startsWith("```")) {
                    jsonProfile = jsonProfile.substring(3);
                }
                if (jsonProfile.endsWith("```")) {
                    jsonProfile = jsonProfile.substring(0, jsonProfile.length() - 3);
                }
                jsonProfile = jsonProfile.trim();
            }
            
            // 3. Parse JSON into DTO
            UpdateUserProfileRequest request = objectMapper.readValue(jsonProfile, UpdateUserProfileRequest.class);
            
            // 4. Update the profile
            UserProfileResponse updatedProfile = userProfileService.updateProfile(token, request);
            
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            log.error("Failed to process resume upload: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process resume: " + e.getMessage(), e);
        }
    }
}
