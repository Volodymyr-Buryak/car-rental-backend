package com.carhub.carrental.user.integration.rabbitmq.event;

import lombok.extern.slf4j.Slf4j;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;
import org.springframework.amqp.listener.ListenerExecutionFailedException;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import com.carhub.carrental.user.integration.rabbitmq.config.RabbitMqRetryableExceptionClassifier;

@Slf4j
@Component("keycloakRabbitMqErrorHandler")
@RequiredArgsConstructor
public class KeycloakRabbitMqErrorHandler implements RabbitListenerErrorHandler {

    private final RabbitMqRetryableExceptionClassifier classifier;

    @Override
    public Object handleError(
            Message amqpMessage,
            Channel channel,
            org.springframework.messaging.Message<?> message,
            ListenerExecutionFailedException exception
    ) throws Exception {
        if (channel == null) throw exception;

        var properties = amqpMessage.getMessageProperties();
        long deliveryTag = properties.getDeliveryTag();

        if (classifier.isRetryable(exception)) {
            log.warn(
                    "Keycloak event will be retried. queue={}, messageId={}",
                    properties.getConsumerQueue(), properties.getMessageId(), exception
            );
            channel.basicReject(deliveryTag, true);
        } else {
            log.error(
                    "Keycloak event rejected. queue={}, messageId={}",
                    properties.getConsumerQueue(), properties.getMessageId(), exception
            );
            channel.basicReject(deliveryTag, false);
        }
        return null;
    }

}