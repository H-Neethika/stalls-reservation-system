package com.booking.booking_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ProblemDetailsHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleRse(ResponseStatusException ex, HttpServletRequest req) {
    HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
    HttpStatus effective = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    String reason = ex.getReason();
    Map<String, Object> body = baseBody(effective, reason, req);
    // Pass through detail/errors map if provided via reason JSON (optional pattern)
    if (reason != null && reason.startsWith("{")) {
      try {
        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(reason);
        if (node.has("detail")) {
          body.put("detail", node.get("detail").asText());
        }
        if (node.has("errors")) {
          body.put("errors", node.get("errors"));
        }
      } catch (Exception ignored) {
      }
    }
    return ResponseEntity
        .status(ex.getStatusCode())
        .contentType(MediaType.valueOf("application/problem+json"))
        .body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
    Map<String, Object> errors = ex.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a, LinkedHashMap::new));

    Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validation failed", req);
    body.put("errors", errors);

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.valueOf("application/problem+json"))
        .body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex, HttpServletRequest req) {
    Map<String, Object> body = baseBody(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.valueOf("application/problem+json"))
        .body(body);
  }

  private Map<String, Object> baseBody(HttpStatus status, String detail, HttpServletRequest req) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", status.value());
    body.put("title", status.getReasonPhrase());
    body.put("detail", detail);
    body.put("instance", req.getRequestURI());
    body.put("timestamp", Instant.now().toString());
    Object cid = req.getAttribute(CorrelationIdFilter.CORRELATION_ID_ATTR);
    if (cid != null) {
      body.put("correlation_id", cid.toString());
    }
    return body;
  }
}
