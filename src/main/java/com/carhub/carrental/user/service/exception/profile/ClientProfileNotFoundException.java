package com.carhub.carrental.user.service.exception.profile;

import java.util.UUID;
import com.carhub.carrental.common.exception.NotFoundException;

public class ClientProfileNotFoundException extends NotFoundException {
    private static final String MESSAGE_TEMPLATE = "Client profile not found for identity: %s";

    public ClientProfileNotFoundException(UUID identityId) {
        super("error.client-profile.not-found.detail", MESSAGE_TEMPLATE.formatted(identityId), identityId);
    }
}
