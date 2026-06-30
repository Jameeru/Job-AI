package com.jobai.domain.job.repository;

import com.jobai.domain.job.entity.Job;
import com.jobai.domain.job.entity.JobSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {

    Optional<Job> findByExternalIdAndSource(String externalId, JobSource source);

    boolean existsByExternalIdAndSource(String externalId, JobSource source);

    /** Jobs eligible for application: scored, not rejected. */
    @Query("SELECT j FROM Job j WHERE j.isRejected = false AND j.matchScore >= :minScore ORDER BY j.matchScore DESC")
    List<Job> findEligibleJobs(@Param("minScore") BigDecimal minScore);

    /** Paginated job listing for dashboard with optional filters. */
    @Query("""
        SELECT j FROM Job j
        WHERE (:source IS NULL OR j.source = :source)
          AND (:rejected IS NULL OR j.isRejected = :rejected)
          AND (:minScore IS NULL OR j.matchScore >= :minScore)
        ORDER BY j.scrapedAt DESC
    """)
    Page<Job> findAllWithFilters(
        @Param("source") JobSource source,
        @Param("rejected") Boolean rejected,
        @Param("minScore") BigDecimal minScore,
        Pageable pageable
    );

    /** Count jobs scraped since a given time. */
    long countByScrapedAtAfter(Instant since);

    /** Count matched (non-rejected) jobs. */
    long countByIsRejectedFalse();

    List<Job> findByIsRejectedFalseAndMatchScoreGreaterThanEqualOrderByMatchScoreDesc(BigDecimal minScore);

    /** Paginated list of non-rejected jobs, ordered by match score descending. */
    Page<Job> findByIsRejectedFalseOrderByMatchScoreDesc(Pageable pageable);
}
