package com.carhub.carrental.user.service.exception;

import java.util.UUID;

public class ProfileNotFoundException extends RuntimeException {
    private static final String MESSAGE = "Client profile not found for identity: %s";

    public ProfileNotFoundException(UUID identityId) {
        super(MESSAGE.formatted(identityId));
    }
}
