package com.user.userservice.security;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.user.userservice.domain.AuthProvider;
import com.user.userservice.domain.Role;
import com.user.userservice.domain.User;
import com.user.userservice.repository.UserRepository;

@Component
public class ExternalOAuth2UserProcessor {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public ExternalOAuth2UserProcessor(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User ensureUser(String registrationId, String email, String displayName) {
		return userRepository.findByEmail(email)
				.orElseGet(() -> createUser(registrationId, email, displayName));
	}

	/**
	 * Find user by email without creating if not exists.
	 * Used during signup validation to check if user already exists.
	 */
	public User findUser(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}

	/**
	 * Create a new user. Used only after validation passes.
	 */
	@Transactional
	public User createUserIfNotExists(String registrationId, String email, String displayName) {
		return userRepository.findByEmail(email)
				.orElseGet(() -> createUser(registrationId, email, displayName));
	}

	private User createUser(String registrationId, String email, String displayName) {
		String generatedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
		String name = StringUtils.hasText(displayName) ? displayName : email;
		String organization = (StringUtils.hasText(registrationId) ? registrationId : "external") + "-user";

		// Determine auth provider from registrationId
		AuthProvider authProvider;
		if ("github".equalsIgnoreCase(registrationId)) {
			authProvider = AuthProvider.GITHUB;
		} else if ("google".equalsIgnoreCase(registrationId)) {
			authProvider = AuthProvider.GOOGLE;
		} else {
			authProvider = AuthProvider.LOCAL;
		}

		User user = User.builder()
				.name(name)
				.email(email)
				.password(generatedPassword)
				.organizationName(organization)
				.role(Role.VENDOR)
				.authProvider(authProvider)
				.build();
		return userRepository.save(user);
	}
}
