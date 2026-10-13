package com.carhub.carrental.user.service;

import java.util.UUID;
import java.util.Optional;
import com.carhub.carrental.user.service.dto.IdentityUserResponse;

public interface IdentityProvider {
    Optional<IdentityUserResponse> findById(UUID identityId);
}