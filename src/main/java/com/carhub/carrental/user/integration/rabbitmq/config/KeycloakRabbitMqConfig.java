package com.carhub.carrental.user.integration.rabbitmq.config;

import java.util.List;
import com.carhub.carrental.user.service.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import com.carhub.carrental.user.service.exception.IdentityEmailMismatchException;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KeycloakRabbitMqProperties.class)
public class KeycloakRabbitMqConfig {

    private final KeycloakRabbitMqProperties properties;

    @Bean
    public SimpleRabbitListenerContainerFactory keycloakRabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer
    ) {
        var factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setAcknowledgeOnError(false);
        return factory;
    }

    @Bean
    public RabbitMqRetryableExceptionClassifier keycloakRetryableExceptionClassifier() {
        return new RabbitMqRetryableExceptionClassifier(
                List.of(
                        UserNotFoundException.class,
                        IdentityEmailMismatchException.class,
                        TransientDataAccessException.class,
                        DataAccessResourceFailureException.class
                )
        );
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange keycloakIdentityExchange() {
        return new TopicExchange(properties.exchange());
    }

    @Bean
    public TopicExchange keycloakErrorExchange() {
        return new TopicExchange(properties.errorExchange());
    }

    @Bean
    public Queue keycloakAdminEventsQueue() {
        return reliableQuorumQueue(properties.queues().admin().name())
                .deadLetterExchange(properties.errorExchange())
                .build();
    }

    @Bean
    public Queue keycloakClientEventsQueue() {
        return reliableQuorumQueue(properties.queues().client().name())
                .deadLetterExchange(properties.errorExchange())
                .build();
    }

    @Bean
    public Queue keycloakErrorQueue() {
        return QueueBuilder
                .durable(properties.queues().error().name())
                .quorum()
                .build();
    }

    @Bean
    public Declarables keycloakAdminBindings() {
        return createBindings(
                keycloakAdminEventsQueue(),
                keycloakIdentityExchange(),
                properties.queues().admin().bindingKeys()
        );
    }

    @Bean
    public Declarables keycloakClientBindings() {
        return createBindings(
                keycloakClientEventsQueue(),
                keycloakIdentityExchange(),
                properties.queues().client().bindingKeys()
        );
    }

    @Bean
    public Declarables keycloakErrorBindings() {
        return createBindings(
                keycloakErrorQueue(),
                keycloakErrorExchange(),
                properties.queues().error().bindingKeys()
        );
    }

    private Declarables createBindings(Queue queue, TopicExchange exchange, List<String> routingKeys) {
        return new Declarables(routingKeys.stream()
                .map(key -> BindingBuilder.bind(queue).to(exchange).with(key))
                .toList()
        );
    }

    private QueueBuilder reliableQuorumQueue(String name) {
        return QueueBuilder
                .durable(name)
                .quorum()
                .withArgument("x-delayed-retry-type", "failed")
                .withArgument("x-delayed-retry-min", Math.toIntExact(properties.retry().delay().toMillis()))
                .withArgument("x-delayed-retry-max", Math.toIntExact(properties.retry().maxDelay().toMillis()))
                .deliveryLimit(properties.retry().maxRetries())
                .overflow(QueueBuilder.Overflow.rejectPublish)
                .withArgument("x-dead-letter-strategy", "at-least-once");
    }
}
