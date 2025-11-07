package com.user.userservice.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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

	private final JwtService jwtService;
	private final UserRepository userRepository;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final String frontendSuccessUrl;

	public OAuth2LoginSuccessHandler(JwtService jwtService,
			UserRepository userRepository,
			CustomOAuth2UserService customOAuth2UserService,
			@Value("${app.oauth2.frontend-success-url}") String frontendSuccessUrl) {
		this.jwtService = jwtService;
		this.userRepository = userRepository;
		this.customOAuth2UserService = customOAuth2UserService;
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

		var user = userRepository.findByEmail(email);
		if (user.isEmpty()) {
			// Ensure user is provisioned (should already occur in service)
			customOAuth2UserService.ensureUserProvisioned(oauth2Token);
			user = userRepository.findByEmail(email);
		}

		if (user.isEmpty()) {
			response.sendRedirect(frontendSuccessUrl + "?error=user_not_found");
			return;
		}

		String accessToken = jwtService.generateAccessToken(user.get());
		String refreshToken = jwtService.generateRefreshToken(user.get());
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
