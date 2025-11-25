package com.user.userservice.config;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.util.StringUtils;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.user.userservice.domain.Role;
import com.user.userservice.security.UserPrincipal;

@Configuration
public class AuthorizationServerConfig {

    @Value("${app.oauth2.client-id}")
    private String defaultClientId;

    @Value("${app.oauth2.client-secret}")
    private String defaultClientSecret;

    @Value("${app.oauth2.redirect-uris}")
    private String defaultRedirectUris;

    @Value("${app.oauth2.post-logout-redirect-uris}")
    private String defaultPostLogoutRedirectUris;

    @Bean
    public RegisteredClientRepository registeredClientRepository(PasswordEncoder passwordEncoder,
                                                                 JwtProperties jwtProperties) {
        RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(defaultClientId)
                .clientSecret(passwordEncoder.encode(defaultClientSecret))
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .redirectUris(uris -> uris.addAll(parseUris(defaultRedirectUris)))
                .postLogoutRedirectUris(uris -> uris.addAll(parseUris(defaultPostLogoutRedirectUris)))
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("users.read")
                .scope("users.write")
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)
                        .build())
                .tokenSettings(TokenSettings.builder()
                        .reuseRefreshTokens(false)
                        .accessTokenTimeToLive(jwtProperties.accessTokenTtl())
                        .refreshTokenTimeToLive(jwtProperties.refreshTokenTtl())
                        .build());

        return new InMemoryRegisteredClientRepository(builder.build());
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        return Jwks.jwkSource();
    }

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new InMemoryOAuth2AuthorizationService();
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService() {
        return new InMemoryOAuth2AuthorizationConsentService();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(JwtProperties jwtProperties) {
        return AuthorizationServerSettings.builder()
                .issuer(jwtProperties.issuer())
                .build();
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (context.getPrincipal().getPrincipal() instanceof UserPrincipal userPrincipal) {
                context.getClaims().claim("userId", userPrincipal.getUserId());
                context.getClaims().claim("role", userPrincipal.user().getRole().name());
                context.getClaims().claim("name", userPrincipal.user().getName());
                context.getClaims().claim("organization", userPrincipal.user().getOrganizationName());
            } else {
                context.getPrincipal().getAuthorities().stream()
                        .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                        .findFirst()
                        .map(authority -> Role.valueOf(authority.getAuthority().substring("ROLE_".length()))).ifPresent(role -> context.getClaims().claim("role", role.name()));
            }
        };
    }

    private Set<String> parseUris(String uris) {
        Set<String> values = StringUtils.commaDelimitedListToSet(uris);
        values.removeIf(value -> !StringUtils.hasText(value));
        return values;
    }
}
