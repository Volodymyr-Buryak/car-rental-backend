package com.carhub.carrental.user.integration.rabbitmq.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import com.carhub.carrental.user.service.exception.IdentityProviderException;

@RequiredArgsConstructor
public class RabbitMqRetryableExceptionClassifier {

    private final List<Class<? extends Throwable>> retryableExceptions;

    public boolean isRetryable(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (isRetryableException(cause)) {
                return true;
            }
            if (cause instanceof IdentityProviderException) {
                return false;
            }
        }
        return false;
    }

    private boolean isRetryableException(Throwable exception) {
        return retryableExceptions.stream()
                .anyMatch(type -> type.isInstance(exception));
    }
}
