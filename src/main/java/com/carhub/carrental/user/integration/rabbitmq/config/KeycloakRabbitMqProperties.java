package com.carhub.carrental.user.integration.rabbitmq.config;

import java.util.List;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.integration.keycloak.rabbitmq")
public record KeycloakRabbitMqProperties(
        String exchange,
        String errorExchange,
        Retry retry,
        Queues queues
) {
    public record Queues(
            QueueSettings admin,
            QueueSettings user,
            QueueSettings error
    ) {}

    public record Retry(
            int maxRetries,
            Duration delay,
            Duration maxDelay
    ) {}

    public record QueueSettings(
            String name,
            List<String> bindingKeys
    ) {}
}
