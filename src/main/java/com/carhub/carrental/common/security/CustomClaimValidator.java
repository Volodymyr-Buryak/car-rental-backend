package com.carhub.carrental.common.security;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;

@Component
public class CustomClaimValidator implements OAuth2TokenValidator<Jwt> {
    private static final OAuth2Error INVALID_SUBJECT = new OAuth2Error(
            "invalid_token",
            "Invalid JWT subject",
            "https://cosmo-cats.market/errors/invalid-subject"
    );

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String subject = token.getSubject();
        if (subject == null || subject.isBlank()) {
            return OAuth2TokenValidatorResult.failure(INVALID_SUBJECT);
        }

        try {
            UUID.fromString(subject);
            return OAuth2TokenValidatorResult.success();
        } catch (IllegalArgumentException ex) {
            return OAuth2TokenValidatorResult.failure(INVALID_SUBJECT);
        }
    }
}