package com.carhub.carrental.user.service.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    private static final String MESSAGE_TEMPLATE = "Local user not found for identity %s";
    public UserNotFoundException(UUID identityId) {
        super(MESSAGE_TEMPLATE.formatted(identityId));
    }
}
