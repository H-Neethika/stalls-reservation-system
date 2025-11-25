package com.user.userservice.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import com.user.userservice.domain.User;

@Service
public class CustomOidcUserService extends OidcUserService {

	private final ExternalOAuth2UserProcessor externalUserProcessor;

	public CustomOidcUserService(ExternalOAuth2UserProcessor externalUserProcessor) {
		this.externalUserProcessor = externalUserProcessor;
	}

	@Override
	public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
		OidcUser oidcUser = super.loadUser(userRequest);
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		String email = oidcUser.getEmail();

		User user = externalUserProcessor.findUser(email);

		if (user != null) {
			UserPrincipal principal = new UserPrincipal(user);
			return new DefaultOidcUser(principal.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo(),
					"email");
		} else {
			Map<String, String> claims = new HashMap<>();
			claims.put("email", email);
			claims.put("name", oidcUser.getFullName());
			claims.put("registrationId", registrationId);

			return new DefaultOidcUser(List.of(), oidcUser.getIdToken(), oidcUser.getUserInfo(), "email");
		}
	}
}
