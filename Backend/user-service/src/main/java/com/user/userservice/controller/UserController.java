package com.user.userservice.controller;

import java.net.URI;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.user.userservice.domain.Role;
import com.user.userservice.dto.AuthResponse;
import com.user.userservice.dto.LoginRequest;
import com.user.userservice.dto.RegisterUserRequest;
import com.user.userservice.dto.UserResponse;
import com.user.userservice.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
		UserResponse response = userService.register(request);
		URI location = URI.create("/users/" + response.id());
		return ResponseEntity.created(location).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(userService.login(request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
		return ResponseEntity.ok(userService.getById(id));
	}

	@GetMapping("/role/{role}")
	@PreAuthorize("hasRole('ORGANIZER')")
	public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable String role) {
		Role parsedRole = parseRole(role);
		return ResponseEntity.ok(userService.getByRole(parsedRole));
	}

	private Role parseRole(String role) {
		try {
			return Role.valueOf(role.toUpperCase(Locale.ENGLISH));
		} catch (IllegalArgumentException ex) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + role);
		}
	}
}
