package com.carhub.carrental.user.service.exception.user;

import java.util.UUID;
import com.carhub.carrental.common.exception.ApplicationException;

public class IdentityEmailMismatchException extends ApplicationException {
    private static final String MESSAGE_TEMPLATE = "Local email does not match the email verification event for user %s";

    public IdentityEmailMismatchException(UUID identityId) {
        super("error.user.identity-email-mismatch.detail", MESSAGE_TEMPLATE.formatted(identityId), identityId);
    }
}
