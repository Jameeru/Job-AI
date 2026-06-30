package com.jobai.interfaces.rest;

import com.jobai.application.service.JobSearchOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Admin endpoint to manually trigger the job search pipeline.
 *
 * <p>Useful for development and ad-hoc runs without waiting for the cron schedule.
 * In production this should be protected by an additional admin role check.
 */
@Slf4j
@Tag(name = "Admin", description = "Admin / operational endpoints")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class OrchestratorController {

    private final JobSearchOrchestrator orchestrator;

    @Operation(summary = "Manually trigger the job search pipeline (scrape + AI analysis)")
    @PostMapping("/run-job-search")
    public ResponseEntity<Map<String, String>> triggerJobSearch() {
        log.info("Manual job search pipeline trigger via API");
        // Run asynchronously to avoid HTTP timeout on large scrapes
        new Thread(orchestrator::runNow, "manual-job-search").start();
        return ResponseEntity.ok(Map.of(
            "status", "started",
            "message", "Job search pipeline is running in the background. Check logs for progress."
        ));
    }
}
