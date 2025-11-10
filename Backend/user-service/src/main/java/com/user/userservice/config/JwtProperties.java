package com.user.userservice.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
		@NotBlank(message = "JWT issuer must be provided")
		String issuer,

		@NotNull(message = "JWT access token TTL must be provided")
		Duration accessTokenTtl,

		@NotNull(message = "JWT refresh token TTL must be provided")
		Duration refreshTokenTtl
) {
}
