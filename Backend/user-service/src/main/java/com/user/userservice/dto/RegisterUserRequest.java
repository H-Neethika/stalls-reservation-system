package com.user.userservice.dto;

import com.user.userservice.domain.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
		@NotBlank(message = "Name is required")
		@Size(max = 100, message = "Name must be at most 100 characters")
		String name,

		@NotBlank(message = "Email is required")
		@Email(message = "Email must be valid")
		String email,

		@NotBlank(message = "Password is required")
		@Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
		String password,

		@NotBlank(message = "Organization name is required")
		@Size(max = 150, message = "Organization name must be at most 150 characters")
		String organizationName,

		@NotNull(message = "Role is required")
		Role role
) {
}
