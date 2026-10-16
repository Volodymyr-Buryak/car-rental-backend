package com.carhub.carrental.user.integration.keycloak.exception;

import lombok.Getter;

@Getter
public class IdentityProviderException extends RuntimeException {
    private final Integer statusCode;

    public IdentityProviderException(String message, Throwable cause) {
        this(message, null, cause);
    }

    public IdentityProviderException(String message, Integer statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }
}
