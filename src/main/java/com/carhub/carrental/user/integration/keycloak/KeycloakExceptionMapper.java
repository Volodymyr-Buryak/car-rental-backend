package com.carhub.carrental.user.integration.keycloak;

import java.util.List;
import java.util.UUID;
import java.net.SocketException;
import javax.net.ssl.SSLException;
import java.net.UnknownHostException;
import java.net.SocketTimeoutException;
import jakarta.ws.rs.ProcessingException;
import java.net.http.HttpTimeoutException;
import jakarta.ws.rs.WebApplicationException;
import java.security.cert.CertificateException;
import org.springframework.stereotype.Component;
import com.carhub.carrental.user.integration.keycloak.exception.IdentityProviderException;
import com.carhub.carrental.user.integration.keycloak.exception.IdentityProviderUnavailableException;

@Component
public class KeycloakExceptionMapper {

    private static final List<Class<? extends Throwable>> PERMANENT_FAILURES = List.of(
            SSLException.class,
            CertificateException.class,
            InterruptedException.class
    );

    private static final List<Class<? extends Throwable>> TRANSIENT_FAILURES = List.of(
            SocketException.class,
            SocketTimeoutException.class,
            UnknownHostException.class,
            HttpTimeoutException.class
    );

    private static final String HTTP_FAILURE_MESSAGE = "Keycloak returned HTTP %d while reading user %s";
    private static final String PROCESSING_FAILURE_MESSAGE = "Keycloak failed while reading user %s: %s";

    public IdentityProviderException mapHttpException(UUID identityId, WebApplicationException cause) {
        int status = cause.getResponse().getStatus();
        if (isTemporaryHttpFailure(status)) {
            return new IdentityProviderUnavailableException(
                    HTTP_FAILURE_MESSAGE.formatted(status, identityId),
                    status,
                    cause
            );
        }
        return new IdentityProviderException(
                HTTP_FAILURE_MESSAGE.formatted(status, identityId),
                status,
                cause
        );
    }

    public IdentityProviderException mapProcessingException(UUID identityId, ProcessingException cause) {
        String rootCauseName = rootCause(cause).getClass().getSimpleName();
        if (isTemporaryTransportFailure(cause)) {
            return new IdentityProviderUnavailableException(
                    PROCESSING_FAILURE_MESSAGE.formatted(identityId, rootCauseName),
                    cause
            );
        }
        return new IdentityProviderException(
                PROCESSING_FAILURE_MESSAGE.formatted(identityId, rootCauseName),
                cause
        );
    }

    private static boolean isTemporaryHttpFailure(int status) {
        return switch (status) {
            case 408, 429, 500, 502, 503, 504 -> true;
            default -> false;
        };
    }

    private static boolean isTemporaryTransportFailure(Throwable exception) {
        boolean transportFailure = false;
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (isPermanentFailure(cause)) {
                return false;
            }
            if (isTransientFailure(cause)) {
                transportFailure = true;
            }
        }
        return transportFailure;
    }

    private static boolean isPermanentFailure(Throwable cause) {
        return PERMANENT_FAILURES.stream().anyMatch(type -> type.isInstance(cause));
    }

    private static boolean isTransientFailure(Throwable cause) {
        return TRANSIENT_FAILURES.stream().anyMatch(type -> type.isInstance(cause));
    }

    private static Throwable rootCause(Throwable exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
