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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Candidate profile management")
@SecurityRequirement(name = "Bearer Authentication")
public class UserProfileController {

    private final UserProfileService userProfileService;

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
}
