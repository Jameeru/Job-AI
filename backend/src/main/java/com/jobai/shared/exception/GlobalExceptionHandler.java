package com.jobai.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized exception handler for all REST controllers.
 *
 * <p>Catches domain exceptions and Spring validation errors, returning
 * a consistent {@link ErrorResponse} JSON payload to the frontend.
 *
 * <p>Never exposes internal stack traces in responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ──────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        ResourceNotFoundException ex, HttpServletRequest request
    ) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .error("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build()
        );
    }

    // ── 409 Conflict ───────────────────────────────────────────────
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
        ConflictException ex, HttpServletRequest request
    ) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse.builder()
                .status(HttpStatus.CONFLICT.value())
                .error("CONFLICT")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build()
        );
    }

    // ── 400 Validation Errors ──────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex, HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> ErrorResponse.FieldError.builder()
                .field(fe.getField())
                .rejectedValue(fe.getRejectedValue() != null ? fe.getRejectedValue().toString() : "null")
                .message(fe.getDefaultMessage())
                .build())
            .collect(Collectors.toList());

        log.warn("Validation failed: {} field errors", fieldErrors.size());

        return ResponseEntity.badRequest().body(
            ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_FAILED")
                .message("Request validation failed. Please check the fieldErrors for details.")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build()
        );
    }

    // ── 500 Internal Server Error ──────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
        Exception ex, HttpServletRequest request
    ) {
        // Log with full stack trace internally, but never expose it in the response
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .build()
        );
    }
}
