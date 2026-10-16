package com.carhub.carrental.user.integration.rabbitmq.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessResourceException;
import com.carhub.carrental.user.service.exception.user.IdentityEmailMismatchException;
import com.carhub.carrental.user.integration.keycloak.exception.IdentityProviderUnavailableException;
import com.carhub.carrental.user.service.exception.user.UserNotFoundException;

class RabbitMqRetryableExceptionClassifierTest {

    private final RabbitMqRetryableExceptionClassifier classifier =
            new KeycloakRabbitMqConfig(null).keycloakRetryableExceptionClassifier();

    @Test
    void temporaryDatabaseFailuresAreRetryableIncludingNestedCauses() {
        assertThat(classifier.isRetryable(new DataAccessResourceFailureException("Database unavailable"))).isTrue();
        assertThat(classifier.isRetryable(new RuntimeException(
                new TransientDataAccessResourceException("Temporary database failure")))).isTrue();
    }

    @Test
    void synchronizationDelaysAreRetryable() {
        var identityId = UUID.randomUUID();
        assertThat(classifier.isRetryable(new UserNotFoundException(identityId))).isTrue();
        assertThat(classifier.isRetryable(new IdentityEmailMismatchException(identityId))).isTrue();
    }

    @Test
    void permanentFailuresAreNotRetryable() {
        assertThat(classifier.isRetryable(new DataIntegrityViolationException("Duplicate email"))).isFalse();
        assertThat(classifier.isRetryable(new IllegalArgumentException("Invalid event"))).isFalse();
    }

    @Test
    void unusedKeycloakClientFailureIsNotRetryable() {
        var failure = new IdentityProviderUnavailableException("Keycloak unavailable", new RuntimeException());
        assertThat(classifier.isRetryable(failure)).isFalse();
    }
}
