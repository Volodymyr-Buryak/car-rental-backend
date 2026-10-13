package com.carhub.carrental.user.service.exception;

import java.util.UUID;

public class EmailNotVerifiedException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Email is not verified for identity user %s";
    public EmailNotVerifiedException(UUID identityId) {
        super(String.format(MESSAGE_TEMPLATE, identityId));
    }
}
