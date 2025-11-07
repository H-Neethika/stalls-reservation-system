package com.user.userservice.security;

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
		User user = externalUserProcessor.ensureUser(registrationId, email, oidcUser.getFullName());
		UserPrincipal principal = new UserPrincipal(user);
		return new DefaultOidcUser(principal.getAuthorities(), oidcUser.getIdToken(), oidcUser.getUserInfo(), "email");
	}
}
