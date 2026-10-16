package com.carhub.carrental.user.integration.keycloak;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.integration.keycloak.admin-client")
public record KeycloakClientProperties(
        String serverUrl,
        String realm,
        String clientId,
        String clientSecret
) {}
