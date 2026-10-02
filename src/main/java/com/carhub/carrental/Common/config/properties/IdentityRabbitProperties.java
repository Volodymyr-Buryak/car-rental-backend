package com.carhub.carrental.Common.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.rabbitmq.identity")
public record IdentityRabbitProperties(
        @NotNull String exchange,
        @NotNull String queue,
        @NotNull String routingKey
) { }