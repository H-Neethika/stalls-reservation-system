package com.user.userservice.service;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.user.userservice.domain.Role;
import com.user.userservice.domain.User;
import com.user.userservice.dto.AuthResponse;
import com.user.userservice.dto.LoginRequest;
import com.user.userservice.dto.RegisterUserRequest;
import com.user.userservice.dto.UserResponse;
import com.user.userservice.exception.InvalidCredentialsException;
import com.user.userservice.exception.UserAlreadyExistsException;
import com.user.userservice.exception.UserNotFoundException;
import com.user.userservice.mapper.UserMapper;
import com.user.userservice.repository.UserRepository;
import com.user.userservice.security.JwtService;
import com.user.userservice.security.UserPrincipal;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public UserService(UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			AuthenticationManager authenticationManager) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	@Transactional
	public UserResponse register(RegisterUserRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new UserAlreadyExistsException("A user with email %s already exists".formatted(request.email()));
		}

		User user = User.builder()
				.name(request.name())
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.organizationName(request.organizationName())
				.role(request.role())
				.build();

		User savedUser = userRepository.save(user);
		return UserMapper.toResponse(savedUser);
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		try {
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
					request.email(), request.password());
			UserPrincipal principal = (UserPrincipal) authenticationManager.authenticate(authenticationToken)
					.getPrincipal();
			User user = principal.getUser();
			String accessToken = jwtService.generateAccessToken(user);
			String refreshToken = jwtService.generateRefreshToken(user);
			return AuthResponse.of(accessToken, refreshToken, UserMapper.toResponse(user));
		} catch (BadCredentialsException ex) {
			throw new InvalidCredentialsException("Invalid email or password");
		}
	}

	@Transactional(readOnly = true)
	public UserResponse getById(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
		return UserMapper.toResponse(user);
	}

	@Transactional(readOnly = true)
	public List<UserResponse> getByRole(Role role) {
		return userRepository.findByRole(role)
				.stream()
				.map(UserMapper::toResponse)
				.toList();
	}
}
