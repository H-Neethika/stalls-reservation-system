package com.user.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

@Slf4j
@Configuration
public class ProxyConfig {

    @Value("${http.proxy.host:#{null}}")
    private String proxyHost;

    @Value("${http.proxy.port:#{null}}")
    private Integer proxyPort;

    @Value("${http.non.proxy.hosts:localhost|127.*|[::1]}")
    private String nonProxyHosts;

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient() {
        if (StringUtils.hasText(proxyHost) && proxyPort != null) {
            log.info("Configuring OAuth2 client with proxy: {}:{}", proxyHost, proxyPort);
            
            // Set system properties for HTTP client proxy
            System.setProperty("http.proxyHost", extractHost(proxyHost));
            System.setProperty("http.proxyPort", String.valueOf(proxyPort));
            System.setProperty("https.proxyHost", extractHost(proxyHost));
            System.setProperty("https.proxyPort", String.valueOf(proxyPort));
            
            if (StringUtils.hasText(nonProxyHosts)) {
                System.setProperty("http.nonProxyHosts", nonProxyHosts);
            }

            // Return default client - it will use the system proxy properties
            return new DefaultAuthorizationCodeTokenResponseClient();
        } else {
            log.info("No proxy configuration detected. Using default OAuth2 client.");
            return new DefaultAuthorizationCodeTokenResponseClient();
        }
    }

    @Bean
    public WebClient webClient() {
        if (StringUtils.hasText(proxyHost) && proxyPort != null) {
            log.info("Configuring WebClient with proxy: {}:{}", proxyHost, proxyPort);
            
            HttpClient httpClient = HttpClient.create()
                    .proxy(proxy -> {
                        ProxyProvider.Builder builder = proxy
                                .type(ProxyProvider.Proxy.HTTP)
                                .host(extractHost(proxyHost))
                                .port(proxyPort);
                        
                        // Only set nonProxyHosts if it's provided and not empty
                        if (StringUtils.hasText(nonProxyHosts)) {
                            // Escape special regex characters for the nonProxyHosts pattern
                            String escapedNonProxyHosts = escapeRegexSpecialChars(nonProxyHosts);
                            builder.nonProxyHosts(escapedNonProxyHosts);
                        }
                    });

            return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();
        } else {
            log.info("No proxy configuration detected. Using default WebClient.");
            return WebClient.builder().build();
        }
    }

    /**
     * Extract hostname from proxy URL if it includes protocol
     * e.g., "http://10.50.225.222" -> "10.50.225.222"
     */
    private String extractHost(String proxyHost) {
        if (proxyHost.startsWith("http://")) {
            return proxyHost.substring(7);
        } else if (proxyHost.startsWith("https://")) {
            return proxyHost.substring(8);
        }
        return proxyHost;
    }

    /**
     * Escape special regex characters in nonProxyHosts pattern
     * Converts patterns like "localhost|127.*|[::1]|*.local" 
     * to valid regex by escaping the * character
     */
    private String escapeRegexSpecialChars(String pattern) {
        // Replace standalone * with .* for proper regex matching
        // This converts "*.local" to ".*\.local"
        return pattern.replace("*.", ".*\\.");
    }
}
