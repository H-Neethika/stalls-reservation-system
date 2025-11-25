package com.gateway.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:9090}")
    private String jwtIssuerUri;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        // Public endpoints - no authentication required
                        .pathMatchers(HttpMethod.POST, "/api/users/register", "/api/users/login").permitAll()
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // OAuth2 endpoints - public
                        .pathMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                        // WebSocket handshake + SockJS info endpoints - allow without JWT
                        .pathMatchers("/ws-stalls/**").permitAll()

                        // Swagger/OpenAPI endpoints - public
                        .pathMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/USER-SERVICE/v3/api-docs/**",
                                "/BOOKING-SERVICE/v3/api-docs/**",
                                "/EXHIBITION-SERVICE/v3/api-docs/**",
                                "/NOTIFICATION-SERVICE/v3/api-docs/**",
                                "/PAYMENT-SERVICE/v3/api-docs/**",
                                "/USER-SERVICE/swagger-ui/**",
                                "/BOOKING-SERVICE/swagger-ui/**",
                                "/EXHIBITION-SERVICE/swagger-ui/**",
                                "/NOTIFICATION-SERVICE/swagger-ui/**",
                                "/PAYMENT-SERVICE/swagger-ui/**")
                        .permitAll()

                        // Stripe webhook - public (validated by signature)
                        .pathMatchers(HttpMethod.POST, "/api/payment/webhook").permitAll()

                        // Public read endpoints for exhibitions
                        .pathMatchers(HttpMethod.GET, "/api/exhibition/**").permitAll()

                        // ORGANIZER-only endpoints
                        .pathMatchers(HttpMethod.POST, "/api/exhibition/**").hasRole("ORGANIZER")
                        .pathMatchers(HttpMethod.PUT, "/api/exhibition/**").hasRole("ORGANIZER")
                        .pathMatchers(HttpMethod.DELETE, "/api/exhibition/**").hasRole("ORGANIZER")

                        .pathMatchers(HttpMethod.POST, "/api/genre/**").hasRole("ORGANIZER")
                        .pathMatchers(HttpMethod.PUT, "/api/genre/**").hasRole("ORGANIZER")
                        .pathMatchers(HttpMethod.DELETE, "/api/genre/**").hasRole("ORGANIZER")

                        .pathMatchers(HttpMethod.POST, "/api/hall/**").hasRole("ORGANIZER")
                        .pathMatchers(HttpMethod.PUT, "/api/hall/**").hasRole("ORGANIZER")
                        .pathMatchers(HttpMethod.DELETE, "/api/hall/**").hasRole("ORGANIZER")

                        .pathMatchers(HttpMethod.POST, "/api/exhibition-halls/**").hasRole("ORGANIZER")
                        .pathMatchers(HttpMethod.PUT, "/api/exhibition-halls/**").hasRole("ORGANIZER")
                        .pathMatchers(HttpMethod.DELETE, "/api/exhibition-halls/**").hasRole("ORGANIZER")

                        .pathMatchers(HttpMethod.POST, "/api/exhibition-stalls/**").hasRole("ORGANIZER")
                        .pathMatchers(HttpMethod.PUT, "/api/exhibition-stalls/**").hasRole("ORGANIZER")
                        .pathMatchers(HttpMethod.DELETE, "/api/exhibition-stalls/**").hasRole("ORGANIZER")

                        .pathMatchers(HttpMethod.GET, "/api/users/role/**").hasRole("ORGANIZER")

                        // VENDOR-only endpoints
                        .pathMatchers(HttpMethod.POST, "/api/reservation/**").hasRole("VENDOR")
                        .pathMatchers(HttpMethod.POST, "/api/payment/intent").hasRole("VENDOR")

                        // Authenticated endpoints (any role)
                        .pathMatchers("/api/reservation/**").authenticated()
                        .pathMatchers("/api/payment/**").authenticated()
                        .pathMatchers("/api/notification/**").authenticated()
                        .pathMatchers("/api/users/**").authenticated()
                        .pathMatchers("/api/genre/**").authenticated()
                        .pathMatchers("/api/hall/**").authenticated()
                        .pathMatchers("/api/exhibition-halls/**").authenticated()
                        .pathMatchers("/api/exhibition-stalls/**").authenticated()

                        // All other requests require authentication
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // For now, we'll use a simple decoder that trusts the issuer-uri from
        // configuration
        return NimbusReactiveJwtDecoder.withIssuerLocation(jwtIssuerUri).build();
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        return jwt -> {
            try {
                Collection<GrantedAuthority> authorities = new ArrayList<>();

                // Debug logging
                System.out.println("JWT Claims: " + jwt.getClaims());

                // Extract role from JWT claims
                String role = jwt.getClaimAsString("role");
                System.out.println("Extracted role: " + role);

                if (role != null) {
                    String authority = "ROLE_" + role;
                    authorities.add(new SimpleGrantedAuthority(authority));
                    System.out.println("Added authority: " + authority);
                }

                // Extract scopes if present
                Collection<String> scopes = jwt.getClaimAsStringList("scope");
                if (scopes == null) {
                    scopes = jwt.getClaimAsStringList("scp");
                }
                if (scopes != null) {
                    authorities.addAll(scopes.stream()
                            .map(scope -> new SimpleGrantedAuthority("SCOPE_" + scope))
                            .collect(Collectors.toList()));
                }

                System.out.println("Final authorities: " + authorities);

                // Create JwtAuthenticationToken with authorities
                JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt, authorities);
                return Mono.just(authenticationToken);
            } catch (Exception e) {
                System.err.println("Error extracting authorities: " + e.getMessage());
                e.printStackTrace();
                // If there's any error in processing, return an empty authentication
                return Mono.empty();
            }
        };
    }
}
