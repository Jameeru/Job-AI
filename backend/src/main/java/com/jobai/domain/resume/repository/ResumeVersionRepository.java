package com.jobai.domain.resume.repository;

import com.jobai.domain.resume.entity.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, UUID> {

    List<ResumeVersion> findByUserIdOrderByGeneratedAtDesc(UUID userId);

    Optional<ResumeVersion> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);
}
