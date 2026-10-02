package com.carhub.carrental.Common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.carhub.carrental.Common.config.properties.IdentityRabbitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(IdentityRabbitProperties.class)
class RabbitMqConfiguration {

    private final IdentityRabbitProperties properties;

    @Bean
    TopicExchange identityExchange() {
        return ExchangeBuilder
                .topicExchange(properties.exchange())
                .durable(true)
                .build();
    }

    @Bean
    Queue identityQueue() {
        return QueueBuilder
                .durable(properties.queue())
                .build();
    }

    @Bean
    Binding identityBinding(Queue identityQueue, TopicExchange identityExchange) {
        return BindingBuilder
                .bind(identityQueue)
                .to(identityExchange)
                .with(properties.routingKey());
    }

}
