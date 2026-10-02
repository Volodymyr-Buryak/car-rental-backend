package com.carhub.carrental.Common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.carhub.carrental.Common.config.properties.IdentityRabbitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(IdentityRabbitProperties.class)
class UserMessagingConfiguration {

    private final IdentityRabbitProperties properties;

    @Bean
    Declarables userIdentityTopology() {
        TopicExchange exchange = ExchangeBuilder
                .topicExchange(properties.exchange())
                .durable(true)
                .build();

        Queue queue = QueueBuilder
                .durable(properties.queue())
                .build();

        List<Declarable> declarable =
                new ArrayList<>(properties.bindingKeys().size() + 2);

        declarable.add(exchange);
        declarable.add(queue);

        properties.bindingKeys().forEach(bindingKey ->
                declarable.add(
                        BindingBuilder
                                .bind(queue)
                                .to(exchange)
                                .with(bindingKey)
                )
        );

        return new Declarables(declarable);
    }

}