package com.user.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider;

@Slf4j
@Configuration
public class ProxyConfig {

    @Value("${http.proxy.host}")
    private String proxyHost;

    @Value("${http.proxy.port}")
    private Integer proxyPort;

    @Value("${http.non.proxy.hosts}")
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
        } else {
            log.info("No proxy configuration detected. Using default OAuth2 client.");
        }
        return new RestClientAuthorizationCodeTokenResponseClient();
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

    private String extractHost(String proxyHost) {
        if (proxyHost.startsWith("http://")) {
            return proxyHost.substring(7);
        } else if (proxyHost.startsWith("https://")) {
            return proxyHost.substring(8);
        }
        return proxyHost;
    }

    private String escapeRegexSpecialChars(String pattern) {
        return pattern.replace("*.", ".*\\.");
    }
}
