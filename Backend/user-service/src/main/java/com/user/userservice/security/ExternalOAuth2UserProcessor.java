package com.user.userservice.security;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

	private User createUser(String registrationId, String email, String displayName) {
		String generatedPassword = passwordEncoder.encode(UUID.randomUUID().toString());
		String name = StringUtils.hasText(displayName) ? displayName : email;
		String organization = (StringUtils.hasText(registrationId) ? registrationId : "external") + "-user";
		User user = User.builder()
				.name(name)
				.email(email)
				.password(generatedPassword)
				.organizationName(organization)
				.role(Role.VENDOR)
				.build();
		return userRepository.save(user);
	}
}
