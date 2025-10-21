package com.user.userservice.mapper;

import com.user.userservice.domain.User;
import com.user.userservice.dto.UserResponse;

public final class UserMapper {

	private UserMapper() {
	}

	public static UserResponse toResponse(User user) {
		return new UserResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getOrganizationName(),
				user.getRole()
		);
	}
}
