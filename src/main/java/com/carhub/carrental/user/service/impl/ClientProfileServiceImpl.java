package com.carhub.carrental.user.service.impl;

import java.util.UUID;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.carhub.carrental.user.service.ClientProfileService;
import com.carhub.carrental.user.persistence.mapper.UserMapper;
import org.springframework.transaction.annotation.Transactional;
import com.carhub.carrental.user.persistence.entity.ClientProfile;
import com.carhub.carrental.user.persistence.repository.UserRepository;
import com.carhub.carrental.user.persistence.mapper.ClientProfileMapper;
import com.carhub.carrental.user.web.dto.client.profile.ClientProfileResponse;
import com.carhub.carrental.user.persistence.repository.ClientProfileRepository;
import com.carhub.carrental.user.service.impl.completion.ProfileCompletionEvaluator;
import com.carhub.carrental.user.web.dto.client.profile.section.UpdatePersonalInfoRequest;
import com.carhub.carrental.user.service.exception.profile.ClientProfileNotFoundException;

@Service
@RequiredArgsConstructor
public class ClientProfileServiceImpl implements ClientProfileService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final ClientProfileMapper clientProfileMapper;
    private final ProfileCompletionEvaluator completionEvaluator;
    private final ClientProfileRepository clientProfileRepository;

    @Override
    @Transactional
    public void provisionClient(UUID identityId, String email, Instant createdAt) {
        userRepository.insertUserIfNotExists(email, identityId, createdAt);
        clientProfileRepository.insertClientProfileIfNotExists(identityId);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientProfileResponse getClientProfile(UUID identityId) {
        return clientProfileRepository.findByUser_IdentityId(identityId)
                .map(completionEvaluator::evaluate)
                .map(clientProfileMapper::toResponse)
                .orElseThrow(() -> new ClientProfileNotFoundException(identityId));
    }

    @Override
    @Transactional
    public void updatePersonalInfo(UUID identityId, UpdatePersonalInfoRequest request) {
        ClientProfile profile = clientProfileRepository.findByUser_IdentityId(identityId)
                .orElseThrow(() -> new ClientProfileNotFoundException(identityId));

        userMapper.updatePersonalInfo(request, profile.getUser());
        profile.setBirthDate(request.birthDate());
    }


}
