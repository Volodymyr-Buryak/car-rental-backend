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
    public void handleUserEvent(
            Channel channel,
            KeycloakUserEvent event,
            @Header(DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        switch (event.type()) {
            case REGISTER -> {
                log.info("Handling REGISTER event for userId: {}", event.userId());
                clientProfileService.provisionClient(event.userId(), event.details().email(), event.getInstance());
            }
            case VERIFY_EMAIL -> {
                log.info("Handling VERIFY_EMAIL event for userId: {}", event.userId());
                userService.verifyEmail(event.userId(), event.details().email());
            }
            default -> {
                log.warn("Received unknown user event type: {}", event.type());
            }
        }
        channel.basicAck(deliveryTag, false);
    }

}
