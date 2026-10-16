package com.carhub.carrental.user.integration.rabbitmq.config;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRabbitMqConfigTest {

    private static final String PREFIX = "application.integration.keycloak.rabbitmq.";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfiguration.class)
            .withInitializer(context -> {
                try {
                    new YamlPropertySourceLoader()
                            .load("application", new ClassPathResource("application.yaml"))
                            .forEach(source -> context.getEnvironment().getPropertySources().addLast(source));
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            });

    @Test
    void applicationPropertiesDeclareUserQueueWithBindingsAndBrokerRetry() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            var properties = context.getBean(KeycloakRabbitMqProperties.class);
            var config = new KeycloakRabbitMqConfig(properties);
            var queue = config.keycloakUserEventsQueue();

            assertThat(queue.getName()).isEqualTo(context.getEnvironment()
                    .getProperty(PREFIX + "queues.user.name"));
            assertThat(queue.isDurable()).isTrue();
            assertThat(queue.getArguments())
                    .containsEntry("x-queue-type", "quorum")
                    .containsEntry("x-dead-letter-exchange", properties.errorExchange())
                    .containsEntry("x-dead-letter-strategy", "at-least-once")
                    .containsEntry("x-overflow", "reject-publish")
                    .containsEntry("x-delivery-limit", 5)
                    .containsEntry("x-delayed-retry-min", 30000)
                    .containsEntry("x-delayed-retry-max", 120000);
            assertThat(config.keycloakUserBindings().getDeclarablesByType(Binding.class))
                    .allSatisfy(binding -> {
                        assertThat(binding.getDestination()).isEqualTo(queue.getName());
                        assertThat(binding.getExchange()).isEqualTo(properties.exchange());
                    })
                    .extracting(Binding::getRoutingKey)
                    .containsExactlyElementsOf(properties.queues().user().bindingKeys());
            assertThat(config.keycloakAdminEventsQueue().getName()).isEqualTo(properties.queues().admin().name());
            assertThat(config.keycloakErrorQueue().getName()).isEqualTo(properties.queues().error().name());
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(KeycloakRabbitMqProperties.class)
    static class PropertiesConfiguration {}
}
