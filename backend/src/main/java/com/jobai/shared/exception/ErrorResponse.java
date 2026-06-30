package com.jobai.shared.exception;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Standard error response payload returned for all exceptions.
 *
 * <p>Structure ensures frontend can reliably parse error information.
 */
@Data
@Builder
public class ErrorResponse {

    /** HTTP status code */
    private int status;

    /** Short error code (e.g., "RESOURCE_NOT_FOUND", "VALIDATION_FAILED") */
    private String error;

    /** Human-readable message */
    private String message;

    /** API path that triggered the error */
    private String path;

    /** UTC timestamp of the error */
    @Builder.Default
    private Instant timestamp = Instant.now();

    /** Field-level validation errors (present only for 400/422 responses) */
    private List<FieldError> fieldErrors;

    @Data
    @Builder
    public static class FieldError {
        private String field;
        private String rejectedValue;
        private String message;
    }
}
