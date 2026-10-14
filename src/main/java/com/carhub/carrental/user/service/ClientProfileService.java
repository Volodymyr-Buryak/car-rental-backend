package com.carhub.carrental.user.service;

import java.util.UUID;
import java.time.Instant;
import com.carhub.carrental.user.web.dto.client.ClientProfileResponse;

public interface ClientProfileService {
    ClientProfileResponse getProfile(UUID identityId);
    void provisionClient(UUID identityId, String email, Instant createdAt);
}
