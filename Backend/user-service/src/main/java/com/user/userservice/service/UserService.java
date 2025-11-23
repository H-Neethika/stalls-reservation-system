package com.user.userservice.service;

import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.user.userservice.domain.AuthProvider;
import com.user.userservice.domain.Role;
import com.user.userservice.domain.User;
import com.user.userservice.dto.AuthResponse;
import com.user.userservice.dto.LoginRequest;
import com.user.userservice.dto.RegisterUserRequest;
import com.user.userservice.dto.RefreshTokenRequest;
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
	private final org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

	public UserService(UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			AuthenticationManager authenticationManager,
			org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
		this.jwtDecoder = jwtDecoder;
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
				.authProvider(AuthProvider.LOCAL) // Set auth provider for email/password signup
				.build();

		User savedUser = userRepository.save(user);
		return UserMapper.toResponse(savedUser);
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		try {
			UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
					request.email(), request.password());
			UserPrincipal principal = (UserPrincipal) authenticationManager.authenticate(authenticationToken)
					.getPrincipal();
			User user = principal.getUser();
			String accessToken = jwtService.generateAccessToken(user);
			String refreshId = java.util.UUID.randomUUID().toString();
			user.setRefreshTokenId(refreshId);
			userRepository.save(user);
			String refreshToken = jwtService.generateRefreshToken(user, refreshId);
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

	@Transactional
	public AuthResponse refresh(RefreshTokenRequest request) {
		try {
			var jwt = jwtDecoder.decode(request.refreshToken());
			String tokenType = jwt.getClaimAsString("tokenType");
			if (!"REFRESH".equals(tokenType)) {
				throw new InvalidCredentialsException("Invalid refresh token");
			}
			String jti = jwt.getClaimAsString("jti");
			String email = jwt.getSubject();
			User user = userRepository.findByEmail(email)
					.orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
			if (user.getRefreshTokenId() == null || !user.getRefreshTokenId().equals(jti)) {
				throw new InvalidCredentialsException("Invalid refresh token");
			}
			String accessToken = jwtService.generateAccessToken(user);
			String refreshId = java.util.UUID.randomUUID().toString();
			user.setRefreshTokenId(refreshId);
			userRepository.save(user);
			String refreshToken = jwtService.generateRefreshToken(user, refreshId);
			return AuthResponse.of(accessToken, refreshToken, UserMapper.toResponse(user));
		} catch (org.springframework.security.oauth2.jwt.JwtException ex) {
			throw new InvalidCredentialsException("Invalid refresh token");
		}
	}
}
