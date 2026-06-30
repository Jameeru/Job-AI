package com.jobai.domain.application.repository;

import com.jobai.domain.application.entity.Application;
import com.jobai.domain.application.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    List<Application> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Application> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, ApplicationStatus status);

    Optional<Application> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserIdAndJobId(UUID userId, UUID jobId);

    long countByUserId(UUID userId);

    long countByUserIdAndStatus(UUID userId, ApplicationStatus status);

    /** Returns count per status for the dashboard stats widget. */
    @Query("""
        SELECT a.status as status, COUNT(a) as count
        FROM Application a
        WHERE a.userId = :userId
        GROUP BY a.status
    """)
    List<Object[]> countByUserIdGroupedByStatus(@Param("userId") UUID userId);
}
