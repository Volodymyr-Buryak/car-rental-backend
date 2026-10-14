package com.carhub.carrental.user.service.impl;

import java.util.UUID;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.carhub.carrental.user.service.ClientProfileService;
import com.carhub.carrental.user.web.dto.client.ClientProfileResponse;
import com.carhub.carrental.user.persistence.repository.UserRepository;
import com.carhub.carrental.user.persistence.repository.ClientProfileRepository;

@Service
@RequiredArgsConstructor
public class ClientProfileServiceImpl implements ClientProfileService {

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;

    @Override
    @Transactional
    public void provisionClient(UUID identityId, String email, Instant createdAt) {
        userRepository.insertUserIfNotExists(email, identityId, createdAt);
        clientProfileRepository.insertClientProfileIfNotExists(identityId);
    }

    @Override
    public ClientProfileResponse getProfile(UUID identityId) {
        // TODO: Implement the logic to retrieve the client profile based on the identityId
        return null;
    }
}
