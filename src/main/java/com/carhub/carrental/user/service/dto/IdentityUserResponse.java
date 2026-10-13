package com.carhub.carrental.user.service.dto;

import lombok.Builder;
import java.util.UUID;
import java.time.Instant;

@Builder
public record IdentityUserResponse(
        UUID identityId,
        String email,
        Instant createdAt,
        boolean emailVerified
) {}
