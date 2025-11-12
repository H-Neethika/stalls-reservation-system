package com.user.userservice.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Custom OAuth2 Authorization Request Resolver that:
 * 1. Adds prompt parameter to force account selection screen on GitHub/Google
 * OAuth flow
 * 2. Captures mode parameter (signup/signin) from request and stores in session
 */
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2AuthorizationRequestResolver.class);

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public CustomOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository,
            String authorizationRequestBaseUri) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, authorizationRequestBaseUri);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest, request) : null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest, request) : null;
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request) {

        // Capture mode parameter (signup/signin) and store in session
        String mode = request.getParameter("mode");
        if (mode != null && (mode.equals("signup") || mode.equals("signin"))) {
            request.getSession().setAttribute("oauth2_mode", mode);
            log.info("OAuth2 mode set in session: {}", mode);
        } else {
            log.info("No valid mode parameter found, will default to signin");
        }

        Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());

        // Add prompt=select_account to show account selection screen
        // GitHub: Shows "Sign in as..." confirmation screen
        // Google: Shows account selection screen with all logged-in accounts
        additionalParameters.put("prompt", "select_account");

        return OAuth2AuthorizationRequest
                .from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }
}
