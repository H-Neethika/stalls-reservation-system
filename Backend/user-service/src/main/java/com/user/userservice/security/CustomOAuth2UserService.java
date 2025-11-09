package com.user.userservice.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.user.userservice.domain.User;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final ExternalOAuth2UserProcessor externalUserProcessor;

	public CustomOAuth2UserService(ExternalOAuth2UserProcessor externalUserProcessor) {
		this.externalUserProcessor = externalUserProcessor;
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
		OAuth2User oauth2User = delegate.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		String email = extractEmail(userRequest, oauth2User);

		// Check if user exists, but DON'T create yet - let the success handler decide
		User user = externalUserProcessor.findUser(email);

		Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
		attributes.put("email", email);
		attributes.put("registrationId", registrationId);

		if (user != null) {
			// Existing user - add user info to attributes
			UserPrincipal principal = new UserPrincipal(user);
			attributes.put("userId", user.getId());
			attributes.putIfAbsent("name", user.getName());
			return new DefaultOAuth2User(principal.getAuthorities(), attributes, "email");
		} else {
			// New user - just return OAuth2 info, don't create yet
			// The success handler will validate and create if appropriate
			attributes.putIfAbsent("name", extractDisplayName(oauth2User));
			return new DefaultOAuth2User(List.of(), attributes, "email");
		}
	}

	private String extractEmail(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
		String registrationId = userRequest.getClientRegistration().getRegistrationId().toLowerCase();
		String email = oauth2User.<String>getAttribute("email");
		if (StringUtils.hasText(email)) {
			return email;
		}

		if ("github".equals(registrationId)) {
			return fetchGithubEmail(userRequest);
		}

		throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"),
				"Unable to retrieve email address from %s provider".formatted(registrationId));
	}

	private String fetchGithubEmail(OAuth2UserRequest userRequest) {
		String token = userRequest.getAccessToken().getTokenValue();
		RestTemplate restTemplate = new RestTemplate();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
			HttpEntity<Void> entity = new HttpEntity<>(headers);
			ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
					"https://api.github.com/user/emails",
					HttpMethod.GET,
					entity,
					new ParameterizedTypeReference<>() {
					});
			List<Map<String, Object>> emails = response.getBody();
			if (!CollectionUtils.isEmpty(emails)) {
				for (Map<String, Object> entry : emails) {
					if (Boolean.TRUE.equals(entry.get("primary"))) {
						return (String) entry.get("email");
					}
				}
				return (String) emails.get(0).get("email");
			}
		} catch (RestClientException ex) {
			throw new OAuth2AuthenticationException(new OAuth2Error("github_email_error"),
					"Failed to retrieve email from GitHub", ex);
		}
		throw new OAuth2AuthenticationException(new OAuth2Error("email_not_found"),
				"GitHub account does not provide an email address");
	}

	private String extractDisplayName(OAuth2User oauth2User) {
		for (String key : List.of("name", "login", "preferred_username")) {
			String value = oauth2User.getAttribute(key);
			if (StringUtils.hasText(value)) {
				return value;
			}
		}
		return oauth2User.getName();
	}
}
