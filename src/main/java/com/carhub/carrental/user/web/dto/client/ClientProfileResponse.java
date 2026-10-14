package com.carhub.carrental.user.web.dto.client;

import java.time.LocalDate;

public record ClientProfileResponse(
        String avatarUrl,
        String firstName,
        String lastName,
        String middleName,
        String phoneNumber,
        LocalDate birthDate,
        String email,
        boolean emailVerified,
        IdentityDocumentResponse identityDocument,
        DriverLicenseResponse driverLicense
) {}