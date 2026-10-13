package com.carhub.carrental.user.integration.rabbitmq.listener;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.amqp.support.AmqpHeaders;
import com.carhub.carrental.user.service.UserService;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import com.carhub.carrental.user.integration.rabbitmq.event.KeycloakClientEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClientEventListener {

    private final UserService userService;

    @RabbitListener(
            queues = "${application.integration.keycloak.rabbitmq.queues.client.name}",
            containerFactory = "keycloakRabbitListenerContainerFactory",
            errorHandler = "keycloakRabbitMqErrorHandler"
    )
    public void handleClientEvent(
            KeycloakClientEvent event, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        switch (event.type()) {
            case REGISTER -> {
                log.info("Handling REGISTER event for userId: {}", event.userId());
                userService.register(event.userId(), event.details().email(), event.time());
            }
            case VERIFY_EMAIL -> {
                log.info("Handling VERIFY_EMAIL event for userId: {}", event.userId());
                userService.verifyEmail(event.userId());
            }
            default -> {
                log.warn("Received unknown event type: {}", event.type());
            }
        }
        channel.basicAck(deliveryTag, false);
    }
}