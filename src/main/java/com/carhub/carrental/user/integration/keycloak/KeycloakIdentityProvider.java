package com.carhub.carrental.user.integration.keycloak;

import jakarta.ws.rs.*;
import java.util.UUID;
import java.util.Objects;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.stereotype.Service;
import com.carhub.carrental.user.service.IdentityProvider;
import org.keycloak.representations.idm.UserRepresentation;
import com.carhub.carrental.user.service.dto.IdentityUserResponse;

@Service
@RequiredArgsConstructor
public class KeycloakIdentityProvider implements IdentityProvider {

    private final Keycloak keycloakClient;
    private final KeycloakExceptionMapper exceptionMapper;
    private final KeycloakClientProperties keycloakClientProperties;

    @Override
    public Optional<IdentityUserResponse> findById(UUID identityId) {
        try {
            UserRepresentation user = keycloakClient
                    .realm(keycloakClientProperties.realm())
                    .users()
                    .get(identityId.toString())
                    .toRepresentation();

            return Optional.of(toResponse(user));
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            if (status == 404) {
                return Optional.empty();
            }
            throw exceptionMapper.mapHttpException(identityId, e);
        } catch (ProcessingException e) {
            throw exceptionMapper.mapProcessingException(identityId, e);
        }
    }

    private static IdentityUserResponse toResponse(UserRepresentation user) {
        return IdentityUserResponse.builder()
                .identityId(UUID.fromString(user.getId()))
                .email(user.getEmail())
                .createdAt(Instant.ofEpochMilli(Objects.requireNonNull(user.getCreatedTimestamp())))
                .emailVerified(Boolean.TRUE.equals(user.isEmailVerified()))
                .build();
    }

}
