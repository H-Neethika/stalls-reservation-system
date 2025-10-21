package com.user.userservice.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		String message = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(GlobalExceptionHandler::formatFieldError)
				.collect(Collectors.joining("; "));
		ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Validation Failed", message,
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(UserAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleConflict(UserAlreadyExistsException ex, HttpServletRequest request) {
		ErrorResponse response = ErrorResponse.of(HttpStatus.CONFLICT.value(), "Conflict", ex.getMessage(),
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex, HttpServletRequest request) {
		ErrorResponse response = ErrorResponse.of(HttpStatus.NOT_FOUND.value(), "Not Found", ex.getMessage(),
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex,
			HttpServletRequest request) {
		ErrorResponse response = ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", ex.getMessage(),
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
		ErrorResponse response = ErrorResponse.of(ex.getStatusCode().value(), ex.getStatusCode().toString(),
				ex.getReason(),
				request.getRequestURI());
		return ResponseEntity.status(ex.getStatusCode()).body(response);
	}

	@ExceptionHandler(OAuth2AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleOAuth2(OAuth2AuthenticationException ex, HttpServletRequest request) {
		ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "OAuth2 authentication failed",
				ex.getError().getDescription() != null ? ex.getError().getDescription() : ex.getMessage(),
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
			HttpServletRequest request) {
		ErrorResponse response = ErrorResponse.of(HttpStatus.BAD_REQUEST.value(), "Bad Request", ex.getMessage(),
				request.getRequestURI());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
		ErrorResponse response = ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error",
				ex.getMessage(), request.getRequestURI());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	private static String formatFieldError(FieldError fieldError) {
		return fieldError.getField() + " " + fieldError.getDefaultMessage();
	}
}
