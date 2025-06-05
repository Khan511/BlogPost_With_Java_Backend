package com.example.demo.exception;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.example.demo.exception.customeExceptions.AccessDeniedException;
import com.example.demo.exception.customeExceptions.DataIntegrityViolationException;
import com.example.demo.exception.customeExceptions.DuplicateUserException;
import com.example.demo.exception.customeExceptions.FileUploadException;
import com.example.demo.exception.customeExceptions.JwtVerificationException;
import com.example.demo.exception.customeExceptions.MailSendException;
import com.example.demo.exception.customeExceptions.RateLimitExceededException;
import com.example.demo.exception.customeExceptions.UserNotFoundException;

/**
 * This class handles global exceptions for all REST controllers.
 * It extends Spring’s ResponseEntityExceptionHandler to customize error
 * responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Logger to record exception messages (helpful for debugging)
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Base URL for documentation related to errors (helpful for frontend or API
    // consumers)
    private static final String ERROR_DOCS_BASE = "http://localhost:8080/docs/errors/";

    /**
     * Override to automatically add an `instance` URI and trace ID to all
     * ProblemDetail responses.
     * `instance` points to the endpoint URI where the error occurred.
     * `traceId` helps track requests in distributed systems (used for
     * observability).
     */
    @Override
    protected ResponseEntity<Object> createResponseEntity(
            Object responseBody, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        // Check if response body is a ProblemDetail instance
        if (responseBody instanceof ProblemDetail pd) {
            // Extracts URI from request description and removes "uri=" prefix
            String uri = request.getDescription(false).replace("uri=", "");
            pd.setInstance(URI.create(uri)); // Set the exact request path where the error occurred

            // Set a custom property 'traceId' from request header for tracking (optional)
            pd.setProperty("traceId", request.getHeader("X-Trace-Id"));
        }

        // Let the base class handle building the final ResponseEntity
        return super.createResponseEntity(responseBody, headers, status, request);
    }

    /**
     * Handles validation errors thrown when input doesn't meet constraints
     * (e.g., @NotNull, @Size).
     * This method builds a structured response using ProblemDetail.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        // Extract field errors into a map of field -> error message
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField, // Get the field name that caused the error
                        f -> Optional.ofNullable(f.getDefaultMessage()).orElse("Invalid value") // Default message if
                                                                                                // null
                ));

        // Create ProblemDetail with 400 BAD_REQUEST status and a general error message
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Validation failed");

        // Set a human-readable title
        pd.setTitle("Invalid Request");

        // Attach the field error map to the ProblemDetail as a custom property
        pd.setProperty("violations", errors);

        // Provide a URL where developers can read more about this kind of error
        pd.setType(URI.create(ERROR_DOCS_BASE + "validation-error"));

        // Delegate to the common method which sets instance URI and traceId
        return createResponseEntity(pd, headers, status, request);
    }

    // Custom business logic
    @ExceptionHandler(UserNotFoundException.class)
    ProblemDetail handleUserNotFound(UserNotFoundException ex, WebRequest request) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());

        pd.setTitle("Resource Not Found");
        pd.setType(URI.create(ERROR_DOCS_BASE + "not-found"));

        return pd;
    }

    @ExceptionHandler(DuplicateUserException.class)
    ProblemDetail handleDuplicationUser(DuplicateUserException ex, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());

        pd.setTitle("Duplicate Resource");
        pd.setType(URI.create(ERROR_DOCS_BASE + "duplicate-resource"));
        return pd;
    }

    // Security Exception
    @ExceptionHandler(JwtVerificationException.class)
    ProblemDetail handleJwtException(JwtVerificationException ex, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Token validation failed");

        pd.setTitle("Authentication Error");
        pd.setType(URI.create(ERROR_DOCS_BASE + "invalid-token"));
        pd.setProperty("errorCode", "AUTH-401");
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handldAccessDenied(AccessDeniedException ex, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "insufficient-permissions");

        pd.setTitle("Authorization Failure");
        pd.setType(URI.create(ERROR_DOCS_BASE + "insufficient-permissions"));

        return pd;
    }

    // File upload exception
    @ExceptionHandler(FileUploadException.class)
    ProblemDetail handleFileuploadFailed(FileUploadException ex) {

        HttpStatus status = ex.isSizeLimit() ? HttpStatus.PAYLOAD_TOO_LARGE : HttpStatus.BAD_REQUEST;

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, ex.getMessage());

        pd.setTitle("File Upload Error");
        pd.setType(URI.create(ERROR_DOCS_BASE + "file-upload-failure"));
        pd.setProperty("maxFileSize", "10MB");
        return pd;
    }

    // Email Exception
    @ExceptionHandler(MailSendException.class)
    ProblemDetail handleMailSendException(MailSendException ex) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, "Failed to send Email");

        pd.setTitle("Email service Error");
        pd.setType(URI.create(ERROR_DOCS_BASE + "email-failure"));
        return pd;
    }

    // Database exception
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Database Constraint violation");

        pd.setTitle("Data Conflict");
        pd.setType(URI.create(ERROR_DOCS_BASE + "data-conflict"));

        // Extract constriant name if possible
        if (ex.getCause() instanceof ConstraintViolationException cve) {
            pd.setProperty("constraint", cve.getConstraintName());
        }
        return pd;
    }

    @ExceptionHandler(RateLimitExceededException.class)
    ProblemDetail handleRateLimitExceeded(RateLimitExceededException ex) {

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, "Api rate limit exceeded");

        pd.setTitle("Rate Limit exceeded");
        pd.setType(URI.create(ERROR_DOCS_BASE + "rate-limit"));
        pd.setProperty("retryAfter", "60 seconds");
        pd.setProperty("limit", ex.getLimit() + " requests/hour");

        return pd;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleAllExceptions(Exception ex, WebRequest request) {

        // Log full error with trace ID
        String traceId = request.getHeader("X-Trace-Id");
        log.error("Unhandled error [TraceID: {}]: {}", traceId, ex.getMessage(), ex);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Reference: " + traceId);
        return pd;
    }

}
