package com.carhub.carrental.user.integration.rabbitmq.event;

import java.util.UUID;
import lombok.NonNull;
import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KeycloakUserEvent(
        long time,
        @NonNull KeycloakUserEventType type,
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

    public Instant getInstance() {
        return Instant.ofEpochMilli(time);
    }
}