package com.carhub.carrental.user.integration.keycloak;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.stereotype.Service;
import com.carhub.carrental.user.integration.keycloak.mapper.KeycloakUserMapper;

@Service
@RequiredArgsConstructor
public class KeycloakIdentityProvider {

    private final Keycloak keycloakClient;
    private final KeycloakUserMapper userMapper;
    private final KeycloakExceptionMapper exceptionMapper;
    private final KeycloakClientProperties keycloakClientProperties;

//    public Optional<IdentityUserResponse> findById(UUID identityId) {
//        try {
//            UserRepresentation user = keycloakClient.realm(keycloakClientProperties.realm())
//                    .users()
//                    .get(identityId.toString())
//                    .toRepresentation();
//
//            return Optional.ofNullable(user)
//                    .map(userMapper::toResponse);
//        } catch (WebApplicationException e) {
//            if (e.getResponse().getStatus() == 404) {
//                return Optional.empty();
//            }
//            throw exceptionMapper.mapHttpException(identityId, e);
//        } catch (ProcessingException e) {
//            throw exceptionMapper.mapProcessingException(identityId, e);
//        }
//    }
}
