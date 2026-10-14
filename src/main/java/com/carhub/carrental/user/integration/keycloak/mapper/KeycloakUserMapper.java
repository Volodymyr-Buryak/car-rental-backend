package com.carhub.carrental.user.integration.keycloak.mapper;

import java.time.Instant;
import org.mapstruct.Mapper;

@Mapper(imports = Instant.class)
public interface KeycloakUserMapper {
//    @Mapping(target = "identityId", source = "id")
//    @Mapping(target = "createdAt", expression = "java(Instant.ofEpochMilli(user.getCreatedTimestamp()))")
//    @Mapping(target = "emailVerified", source = "emailVerified", defaultValue = "false")
//    IdentityUserResponse toResponse(UserRepresentation user);

}
