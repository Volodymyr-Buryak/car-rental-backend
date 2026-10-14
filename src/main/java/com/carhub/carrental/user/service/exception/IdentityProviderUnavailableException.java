package com.carhub.carrental.user.service.exception;

public class IdentityProviderUnavailableException extends IdentityProviderException {
    public IdentityProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
    public IdentityProviderUnavailableException(String message, int statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }
}
