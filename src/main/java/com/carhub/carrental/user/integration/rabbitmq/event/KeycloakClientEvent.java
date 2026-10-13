package com.carhub.carrental.user.integration.rabbitmq.event;

import java.util.UUID;
import lombok.NonNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakClientEvent(
        long time,
        @NonNull KeycloakClientEventType type,
        @NonNull UUID userId,
        Details details
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Details(
            String email,
            @JsonProperty("previous_email")
            String previousEmail,
            @JsonProperty("updated_email")
            String updatedEmail
    ) {}
}
