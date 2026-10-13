package com.carhub.carrental.user.service.impl;

import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.carhub.carrental.user.persistence.repository.UserRepository;
import com.carhub.carrental.user.service.IdentityProvider;
import com.carhub.carrental.user.service.dto.IdentityUserResponse;

class UserServiceImplTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final IdentityProvider identityProvider = mock(IdentityProvider.class);
    private final UserServiceImpl service = new UserServiceImpl(repository, identityProvider);
    private final UUID identityId = UUID.randomUUID();

    @Test
    void alreadyVerifiedUserDoesNotCallKeycloakOrUpsert() {
        when(repository.existsByIdentityId(identityId)).thenReturn(true);

        service.verifyEmail(identityId);

        verify(repository).markEmailVerified(identityId);
        verify(repository).existsByIdentityId(identityId);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(identityProvider);
    }

    @Test
    void existingUnverifiedUserIsUpdatedWithoutCallingKeycloak() {
        when(repository.markEmailVerified(identityId)).thenReturn(1);

        service.verifyEmail(identityId);

        verify(repository).markEmailVerified(identityId);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(identityProvider);
    }

    @Test
    void missingUserIsSynchronizedWithKeycloakCreationDate() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        var user = IdentityUserResponse.builder()
                .identityId(identityId)
                .email("user@example.com")
                .createdAt(createdAt)
                .emailVerified(true)
                .build();
        when(identityProvider.findById(identityId)).thenReturn(Optional.of(user));

        service.verifyEmail(identityId);

        verify(repository).upsertVerifiedUser("user@example.com", identityId, createdAt);
    }
}
