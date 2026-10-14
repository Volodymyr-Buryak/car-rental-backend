package com.carhub.carrental.user.service.exception;

import java.util.UUID;

public class IdentityEmailMismatchException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Local email does not match the email verification event for user %s";

    public IdentityEmailMismatchException(UUID identityId) {
        super(MESSAGE_TEMPLATE.formatted(identityId));
    }
}
