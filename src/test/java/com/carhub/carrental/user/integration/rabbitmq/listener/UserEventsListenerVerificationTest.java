package com.carhub.carrental.user.integration.rabbitmq.listener;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.UUID;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import com.carhub.carrental.user.integration.rabbitmq.event.KeycloakUserEvent;
import com.carhub.carrental.user.integration.rabbitmq.event.KeycloakUserEventType;
import com.carhub.carrental.user.service.ClientProfileService;
import com.carhub.carrental.user.service.UserService;
import com.carhub.carrental.user.service.exception.user.IdentityEmailMismatchException;
import com.carhub.carrental.user.integration.rabbitmq.config.KeycloakRabbitMqConfig;

class UserEventsListenerVerificationTest {

    private final UserService users = mock(UserService.class);
    private final ClientProfileService profiles = mock(ClientProfileService.class);
    private final Channel channel = mock(Channel.class);
    private final UserEventsListener listener = new UserEventsListener(users, profiles);
    private final UUID identityId = UUID.randomUUID();
    private static final String EMAIL = "user@example.com";

    @Test
    void ackFollowsSuccessfulVerification() throws Exception {
        listener.handleUserEvent(channel, event(), 12L);

        var order = inOrder(users, channel);
        order.verify(users).verifyEmail(identityId, EMAIL);
        order.verify(channel).basicAck(12L, false);
        verifyNoInteractions(profiles);
    }

    @Test
    void failedVerificationDoesNotAckMessage() {
        var failure = new IdentityEmailMismatchException(identityId);
        doThrow(failure).when(users).verifyEmail(identityId, EMAIL);

        assertThatThrownBy(() -> listener.handleUserEvent(channel, event(), 12L)).isSameAs(failure);

        verifyNoInteractions(channel, profiles);
    }

    private KeycloakUserEvent event() {
        return new KeycloakUserEvent(0L, KeycloakUserEventType.VERIFY_EMAIL, identityId,
                new KeycloakUserEvent.Details(EMAIL, null, null));
    }

    @Test
    void missingEmailDoesNotAckOrCallService() {
        var event = new KeycloakUserEvent(0L, KeycloakUserEventType.VERIFY_EMAIL, identityId, null);
        assertThatThrownBy(() -> listener.handleUserEvent(channel, event, 12L))
                .isInstanceOf(NullPointerException.class)
                .satisfies(failure -> assertThat(new KeycloakRabbitMqConfig(null)
                        .keycloakRetryableExceptionClassifier().isRetryable(failure)).isFalse());
        verifyNoInteractions(users, channel, profiles);
    }
}
