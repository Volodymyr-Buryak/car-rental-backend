package com.carhub.carrental.User.input.messaging;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserIdentityEventListener {

    @RabbitListener(queues = "${application.rabbitmq.identity.queue}")
    public void handleUserIdentityCreatedEvent(String event) {
        log.info("Received UserIdentityCreatedEvent: {}", event);
    }


}
