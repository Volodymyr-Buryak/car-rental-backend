package com.carhub.carrental.user.integration.rabbitmq.listener;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.carhub.carrental.user.service.UserService;
import com.carhub.carrental.user.service.ClientProfileService;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.carhub.carrental.user.integration.rabbitmq.event.KeycloakUserEvent;

import static org.springframework.amqp.support.AmqpHeaders.DELIVERY_TAG;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventsListener {

    private final UserService userService;
    private final ClientProfileService clientProfileService;

    @RabbitListener(
            errorHandler = "keycloakRabbitMqErrorHandler",
            containerFactory = "keycloakRabbitListenerContainerFactory",
            queues = "${application.integration.keycloak.rabbitmq.queues.user.name}"
    )
    public void handleUserEvent(Channel channel, KeycloakUserEvent event,
                                @Header(DELIVERY_TAG) long deliveryTag) throws IOException {
        switch (event.type()) {
            case REGISTER -> {
                clientProfileService.provisionClient(event.userId(), event.details().email(), event.getInstance());
            }
            case VERIFY_EMAIL -> {
                userService.verifyEmail(event.userId(), event.details().email());
            }
            default -> log.warn("User event ignored: type={}, userId={}", event.type(), event.userId());
        }
        channel.basicAck(deliveryTag, false);
    }

}
