package com.carhub.carrental.user.service;

import java.util.UUID;
import java.time.Instant;
import com.carhub.carrental.user.web.dto.client.profile.ClientProfileResponse;
import com.carhub.carrental.user.web.dto.client.profile.section.UpdatePersonalInfoRequest;

public interface ClientProfileService {
    ClientProfileResponse getClientProfile(UUID identityId);
    void updatePersonalInfo(UUID identityId, UpdatePersonalInfoRequest request);
    void provisionClient(UUID identityId, String email, Instant createdAt);
}
