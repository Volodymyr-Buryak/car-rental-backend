package com.carhub.carrental.user.service.impl;

import java.util.UUID;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.carhub.carrental.user.service.UserService;
import com.carhub.carrental.user.service.IdentityProvider;
import com.carhub.carrental.user.service.dto.IdentityUserResponse;
import com.carhub.carrental.user.persistence.repository.UserRepository;
import com.carhub.carrental.user.service.exception.EmailNotVerifiedException;
import com.carhub.carrental.user.service.exception.IdentityUserNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final IdentityProvider identityProvider;

    @Override
    public void register(UUID identityId, String email, long registrationTime) {
        if (userRepository.insertUserIfAbsent(email, identityId, Instant.ofEpochMilli(registrationTime)) > 0) {
            log.info("Successfully registered user: {}", identityId);
        }
    }

    @Override
    public void verifyEmail(UUID identityId) {
        if (userRepository.markEmailVerified(identityId) > 0) {
            log.info("Email verified for user: {}", identityId);
            return;
        }
        if (userRepository.existsByIdentityId(identityId)) {
            return;
        }
        log.info("User {} not found locally. Synchronizing from Keycloak", identityId);
        syncVerifiedUser(identityId);
    }

    private void syncVerifiedUser(UUID identityId) {
        IdentityUserResponse user = identityProvider.findById(identityId)
                .orElseThrow(() -> new IdentityUserNotFoundException(identityId));

        if (!user.emailVerified()) {
            throw new EmailNotVerifiedException(identityId);
        }

        userRepository.upsertVerifiedUser(user.email(), identityId, user.createdAt());
        log.info("Verified user synchronized: {}", identityId);
    }
}
