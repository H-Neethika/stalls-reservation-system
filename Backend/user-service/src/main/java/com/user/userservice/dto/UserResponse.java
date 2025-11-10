package com.user.userservice.dto;

import com.user.userservice.domain.Role;

public record UserResponse(
		Long id,
		String name,
		String email,
		String organizationName,
		Role role
) {
}
