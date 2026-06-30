package com.jobai.domain.user.repository;

import com.jobai.domain.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link UserProfile}.
 *
 * <p>The primary lookup key in authentication context is {@code firebaseUid}.
 * Email is used for duplicate-checking during profile creation.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByFirebaseUid(String firebaseUid);

    Optional<UserProfile> findByEmail(String email);

    boolean existsByFirebaseUid(String firebaseUid);

    boolean existsByEmail(String email);

    /**
     * Eagerly fetch profile with all relationships in a single query.
     * Avoids N+1 when assembling a full profile response.
     */
    @Query("""
        SELECT u FROM UserProfile u
        LEFT JOIN FETCH u.skills
        LEFT JOIN FETCH u.projects
        LEFT JOIN FETCH u.certifications
        WHERE u.firebaseUid = :firebaseUid
    """)
    Optional<UserProfile> findByFirebaseUidWithDetails(@Param("firebaseUid") String firebaseUid);

    /** Used by the orchestrator to resolve the first active candidate profile for AI analysis. */
    Optional<UserProfile> findFirstByIsActiveTrue();

    /**
     * Eagerly fetch the first active profile with all relationships.
     * Used by {@code JobSearchOrchestrator} to build the AI prompt context.
     */
    @Query("""
        SELECT u FROM UserProfile u
        LEFT JOIN FETCH u.skills
        LEFT JOIN FETCH u.projects
        LEFT JOIN FETCH u.certifications
        WHERE u.isActive = true
        ORDER BY u.createdAt ASC
    """)
    Optional<UserProfile> findFirstActiveWithDetails();
}

