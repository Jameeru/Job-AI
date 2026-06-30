package com.jobai.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Public health check endpoint.
 *
 * <p>Used by Render to verify the service is up.
 * No authentication required.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Health check")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns 200 OK when the service is running")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "job-ai-backend",
            "version", "1.0.0",
            "timestamp", Instant.now().toString()
        ));
    }
}
