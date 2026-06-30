package com.jobai.interfaces.rest;

import com.jobai.domain.application.dto.ApplicationResponse;
import com.jobai.domain.application.dto.UpdateApplicationStatusRequest;
import com.jobai.domain.application.entity.Application;
import com.jobai.domain.application.entity.ApplicationStatus;
import com.jobai.domain.application.repository.ApplicationRepository;
import com.jobai.domain.resume.entity.ResumeVersion;
import com.jobai.domain.resume.repository.ResumeVersionRepository;
import com.jobai.shared.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints specifically for the Playwright automation agent.
 * 
 * <p>These should be secured via an internal API key or network boundary
 * in production, as they bypass the standard Firebase user context.
 */
@Slf4j
@Tag(name = "Agent", description = "Endpoints for the Playwright Automation Agent")
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final ApplicationRepository applicationRepository;
    private final ResumeVersionRepository resumeVersionRepository;

    @Operation(summary = "Get all PENDING applications")
    @GetMapping("/applications/pending")
    @Transactional(readOnly = true)
    public ResponseEntity<List<Application>> getPendingApplications() {
        // Find all applications where status is PENDING
        // Note: For a real scale, we'd limit this or lock rows.
        List<Application> pendingApps = applicationRepository.findAll().stream()
                .filter(a -> a.getStatus() == ApplicationStatus.PENDING)
                .toList();
        return ResponseEntity.ok(pendingApps);
    }

    @Operation(summary = "Update application status (used by agent to report success/failure)")
    @PatchMapping("/applications/{id}/status")
    @Transactional
    public ResponseEntity<Application> updateApplicationStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        
        app.setStatus(request.status());
        if (request.notes() != null) {
            app.setNotes(request.notes());
        }
        
        return ResponseEntity.ok(applicationRepository.save(app));
    }

    @Operation(summary = "Download resume PDF by ID for uploading to job portal")
    @GetMapping(value = "/resumes/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadResumePdf(@PathVariable UUID id) {
        ResumeVersion rv = resumeVersionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
                
        if (rv.getPdfPath() == null) {
            throw new ResourceNotFoundException("PDF not generated yet for resume " + id);
        }
        
        Path filePath = Paths.get(rv.getPdfPath());
        Resource resource = new FileSystemResource(filePath.toFile());
        
        if (!resource.exists()) {
            throw new ResourceNotFoundException("PDF file not found on disk");
        }
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume_" + id + ".pdf\"")
                .body(resource);
    }
}
