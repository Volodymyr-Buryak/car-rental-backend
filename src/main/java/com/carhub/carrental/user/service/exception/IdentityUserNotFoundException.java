package com.carhub.carrental.user.service.exception;

import java.util.UUID;

public class IdentityUserNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Identity user %s was not found";

    public IdentityUserNotFoundException(UUID identityId) {
        super(String.format(MESSAGE_TEMPLATE, identityId));
    }
}
