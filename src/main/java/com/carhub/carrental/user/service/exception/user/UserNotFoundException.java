package com.carhub.carrental.user.service.exception.user;

import java.util.UUID;
import com.carhub.carrental.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    private static final String MESSAGE_TEMPLATE = "Local user not found for identity %s";
    public UserNotFoundException(UUID identityId) {
        super("error.user.not-found.detail", MESSAGE_TEMPLATE.formatted(identityId), identityId);
    }
}
