package com.user.userservice.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.user.userservice.security.CustomOidcUserService;
import com.user.userservice.security.CustomOAuth2UserService;
import com.user.userservice.security.CustomUserDetailsService;
import com.user.userservice.security.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final CustomUserDetailsService userDetailsService;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomOidcUserService customOidcUserService;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

	public SecurityConfig(CustomUserDetailsService userDetailsService,
			CustomOAuth2UserService customOAuth2UserService,
			CustomOidcUserService customOidcUserService,
			OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
		this.userDetailsService = userDetailsService;
		this.customOAuth2UserService = customOAuth2UserService;
		this.customOidcUserService = customOidcUserService;
		this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
	}

	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
				.oidc(Customizer.withDefaults());
		http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http,
			JwtAuthenticationConverter jwtAuthenticationConverter,
			DaoAuthenticationProvider authenticationProvider) throws Exception {
		http
				.securityMatcher("/users/**")
				.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/v3/api-docs/**",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/webjars/**"
						).permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/api/users/**").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login").permitAll()
						.anyRequest().authenticated())
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
				.authenticationProvider(authenticationProvider);
		return http.build();
	}

	@Bean
	@Order(3)
	public SecurityFilterChain webSecurityFilterChain(HttpSecurity http,
			DaoAuthenticationProvider authenticationProvider,
			OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
				.formLogin(Customizer.withDefaults())
				.oauth2Login(oauth2 -> oauth2
						.tokenEndpoint(token -> token
								.accessTokenResponseClient(accessTokenResponseClient))
						.userInfoEndpoint(userInfo -> userInfo
								.userService(customOAuth2UserService)
								.oidcUserService(customOidcUserService))
						.successHandler(oAuth2LoginSuccessHandler))
				.logout(Customizer.withDefaults())
				.authenticationProvider(authenticationProvider);
		return http.build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setPrincipalClaimName("sub");
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			List<GrantedAuthority> authorities = new ArrayList<>();

			String role = jwt.getClaimAsString("role");
			if (role != null) {
				authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
			}

			Collection<String> scopes = jwt.getClaimAsStringList("scope");
			if (scopes == null) {
				scopes = jwt.getClaimAsStringList("scp");
			}
			if (scopes != null) {
				scopes.stream()
						.map(scope -> "SCOPE_" + scope)
						.map(SimpleGrantedAuthority::new)
						.forEach(authorities::add);
			}
			return authorities;
		});
		return converter;
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:8080"));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cache-Control"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
