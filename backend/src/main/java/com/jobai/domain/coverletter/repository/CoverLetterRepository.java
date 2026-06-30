package com.jobai.domain.coverletter.repository;

import com.jobai.domain.coverletter.entity.CoverLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoverLetterRepository extends JpaRepository<CoverLetter, UUID> {

    List<CoverLetter> findByUserIdOrderByGeneratedAtDesc(UUID userId);

    Optional<CoverLetter> findByIdAndUserId(UUID id, UUID userId);

    Optional<CoverLetter> findByUserIdAndJobId(UUID userId, UUID jobId);
}
