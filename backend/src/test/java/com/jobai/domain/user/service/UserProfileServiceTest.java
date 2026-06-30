package com.jobai.domain.user.service;

import com.google.firebase.auth.FirebaseToken;
import com.jobai.domain.user.dto.CreateUserProfileRequest;
import com.jobai.domain.user.dto.SkillRequest;
import com.jobai.domain.user.dto.UserProfileResponse;
import com.jobai.domain.user.entity.UserProfile;
import com.jobai.domain.user.repository.UserProfileRepository;
import com.jobai.shared.exception.ConflictException;
import com.jobai.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserProfileService}.
 *
 * <p>Uses Mockito — no Spring context, no database. Fast and isolated.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserProfileService")
class UserProfileServiceTest {

    @Mock private UserProfileRepository userProfileRepository;
    @Mock private FirebaseToken mockToken;

    @InjectMocks private UserProfileService userProfileService;

    private static final String FIREBASE_UID = "firebase-uid-test-001";
    private static final String EMAIL        = "test@example.com";

    @BeforeEach
    void setUp() {
        when(mockToken.getUid()).thenReturn(FIREBASE_UID);
        when(mockToken.getEmail()).thenReturn(EMAIL);
    }

    // ── createProfile ─────────────────────────────────────────────

    @Test
    @DisplayName("createProfile — should create and return profile for new user")
    void createProfile_success() {
        when(userProfileRepository.existsByFirebaseUid(FIREBASE_UID)).thenReturn(false);
        when(userProfileRepository.save(any(UserProfile.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        CreateUserProfileRequest request = CreateUserProfileRequest.builder()
            .fullName("Arjun Kumar")
            .graduationYear((short) 2025)
            .skills(List.of(SkillRequest.builder()
                .skillName("Java")
                .category("BACKEND")
                .proficiency("INTERMEDIATE")
                .build()))
            .build();

        UserProfileResponse response = userProfileService.createProfile(mockToken, request);

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getFullName()).isEqualTo("Arjun Kumar");
        assertThat(response.getGraduationYear()).isEqualTo((short) 2025);
        assertThat(response.getSkills()).hasSize(1);
        assertThat(response.getSkills().get(0).getSkillName()).isEqualTo("Java");

        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("createProfile — should throw ConflictException if profile already exists")
    void createProfile_conflict() {
        when(userProfileRepository.existsByFirebaseUid(FIREBASE_UID)).thenReturn(true);

        assertThatThrownBy(() ->
            userProfileService.createProfile(mockToken, CreateUserProfileRequest.builder()
                .fullName("Arjun Kumar").build()))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("already exists");

        verify(userProfileRepository, never()).save(any());
    }

    // ── getProfile ────────────────────────────────────────────────

    @Test
    @DisplayName("getProfile — should return profile when found")
    void getProfile_success() {
        UserProfile profile = UserProfile.builder()
            .firebaseUid(FIREBASE_UID)
            .email(EMAIL)
            .fullName("Arjun Kumar")
            .build();

        when(userProfileRepository.findByFirebaseUidWithDetails(FIREBASE_UID))
            .thenReturn(Optional.of(profile));

        UserProfileResponse response = userProfileService.getProfile(mockToken);

        assertThat(response.getEmail()).isEqualTo(EMAIL);
        assertThat(response.getFullName()).isEqualTo("Arjun Kumar");
    }

    @Test
    @DisplayName("getProfile — should throw ResourceNotFoundException when profile not found")
    void getProfile_notFound() {
        when(userProfileRepository.findByFirebaseUidWithDetails(FIREBASE_UID))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getProfile(mockToken))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deactivateProfile ─────────────────────────────────────────

    @Test
    @DisplayName("deactivateProfile — should set isActive to false")
    void deactivateProfile_success() {
        UserProfile profile = UserProfile.builder()
            .firebaseUid(FIREBASE_UID)
            .email(EMAIL)
            .isActive(true)
            .build();

        when(userProfileRepository.findByFirebaseUidWithDetails(FIREBASE_UID))
            .thenReturn(Optional.of(profile));
        when(userProfileRepository.save(any(UserProfile.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        userProfileService.deactivateProfile(mockToken);

        assertThat(profile.getIsActive()).isFalse();
        verify(userProfileRepository).save(profile);
    }
}
