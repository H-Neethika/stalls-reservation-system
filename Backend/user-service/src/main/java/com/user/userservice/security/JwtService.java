package com.user.userservice.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.user.userservice.config.JwtProperties;
import com.user.userservice.domain.User;

@Service
public class JwtService {

	private final JwtProperties properties;
	private final JwtEncoder jwtEncoder;

	public JwtService(JwtProperties properties, JwtEncoder jwtEncoder) {
		this.properties = properties;
		this.jwtEncoder = jwtEncoder;
	}

	public String generateToken(User user) {
		return generateAccessToken(user);
	}

	public String generateAccessToken(User user) {
		return buildToken(user, properties.accessTokenTtl(), Map.of("scope", List.of("users.read")), "ACCESS");
	}

	public String generateRefreshToken(User user) {
		return buildToken(user, properties.refreshTokenTtl(), Map.of("scope", List.of("users.refresh")), "REFRESH");
	}

	private String buildToken(User user, Duration ttl, Map<String, Object> additionalClaims, String tokenType) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(ttl);

		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.issuer())
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.subject(user.getEmail())
				.claim("userId", user.getId())
				.claim("role", user.getRole().name())
				.claim("name", user.getName())
				.claim("organization", user.getOrganizationName())
				.claim("tokenType", tokenType)
				.claims(claimsMap -> claimsMap.putAll(additionalClaims))
				.build();

		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}
}
