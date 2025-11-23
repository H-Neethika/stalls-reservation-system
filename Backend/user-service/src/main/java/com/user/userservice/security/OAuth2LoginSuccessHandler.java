package com.user.userservice.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.user.userservice.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

	private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final ExternalOAuth2UserProcessor externalUserProcessor;
	private final String frontendSuccessUrl;

	public OAuth2LoginSuccessHandler(JwtService jwtService,
			UserRepository userRepository,
			ExternalOAuth2UserProcessor externalUserProcessor,
			@Value("${app.oauth2.frontend-success-url}") String frontendSuccessUrl) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.externalUserProcessor = externalUserProcessor;
		this.frontendSuccessUrl = frontendSuccessUrl;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		if (!(authentication instanceof OAuth2AuthenticationToken oauth2Token)) {
			response.sendRedirect(frontendSuccessUrl + "?error=unsupported_authentication");
			return;
		}

		String email = oauth2Token.getPrincipal().getAttribute("email");
		if (email == null) {
			response.sendRedirect(frontendSuccessUrl + "?error=email_not_found");
			return;
		}

		// Check OAuth2 mode from session
		String mode = (String) request.getSession().getAttribute("oauth2_mode");
		if (mode == null) {
			mode = "signin"; // Default to signin if not specified
		}

		log.info("OAuth2 authentication for email: {}, mode: {}", email, mode);

		var user = userRepository.findByEmail(email);

		// Validate signup: user must NOT exist
		if ("signup".equals(mode) && user.isPresent()) {
			log.warn("OAuth2 signup failed: account already exists for email: {}", email);
			request.getSession().removeAttribute("oauth2_mode");
			response.sendRedirect(frontendSuccessUrl + "?error=account_already_exists&message=" +
					URLEncoder.encode("An account with this email already exists. Please sign in instead.",
							StandardCharsets.UTF_8));
			return;
		}

		// Validate signin: user must exist
		if ("signin".equals(mode) && user.isEmpty()) {
			log.warn("OAuth2 signin failed: no account found for email: {}", email);
			request.getSession().removeAttribute("oauth2_mode");
			response.sendRedirect(frontendSuccessUrl + "?error=account_not_found&message=" +
					URLEncoder.encode("No account found with this email. Please sign up first.",
							StandardCharsets.UTF_8));
			return;
		}

		// Create user if this is a signup and user doesn't exist
		if ("signup".equals(mode) && user.isEmpty()) {
			String registrationId = oauth2Token.getPrincipal().getAttribute("registrationId");
			String displayName = oauth2Token.getPrincipal().getAttribute("name");
			log.info("Creating new user via OAuth2 signup for email: {}", email);
			externalUserProcessor.createUserIfNotExists(registrationId, email, displayName);
			user = userRepository.findByEmail(email);
		}

		if (user.isEmpty()) {
			log.error("Failed to find or create user for email: {}", email);
			response.sendRedirect(frontendSuccessUrl + "?error=user_not_found");
			return;
		}

		// Clear mode from session
		request.getSession().removeAttribute("oauth2_mode");

		String accessToken = jwtService.generateAccessToken(user.get());
		String refreshId = java.util.UUID.randomUUID().toString();
		user.get().setRefreshTokenId(refreshId);
		userRepository.save(user.get());
		String refreshToken = jwtService.generateRefreshToken(user.get(), refreshId);
		String redirectUrl = buildRedirectUrl(accessToken, refreshToken, user.get().getId());
		response.sendRedirect(redirectUrl);
	}

	private String buildRedirectUrl(String accessToken, String refreshToken, Long userId) {
		StringBuilder builder = new StringBuilder(frontendSuccessUrl);
		builder.append(frontendSuccessUrl.contains("?") ? "&" : "?");
		builder.append("accessToken=").append(encode(accessToken));
		builder.append("&refreshToken=").append(encode(refreshToken));
		builder.append("&userId=").append(userId);
		return builder.toString();
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
