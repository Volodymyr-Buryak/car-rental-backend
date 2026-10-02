package com.carhub.carrental.Common.config.properties;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Validated
@ConfigurationProperties(prefix = "application.rabbitmq.identity")
public record IdentityRabbitProperties(
        @NotBlank String exchange,
        @NotBlank String queue,
        @NotEmpty List<@NotBlank String> bindingKeys
) {}