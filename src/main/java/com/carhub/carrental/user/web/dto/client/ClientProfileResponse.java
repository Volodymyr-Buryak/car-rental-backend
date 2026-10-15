package com.carhub.carrental.user.web.dto.client;

public record ClientProfileResponse(
        PersonalInfoResponse personalInfo,
        AccountResponse account,
        IdentityDocumentResponse identityDocument,
        DriverLicenseResponse driverLicense,
        ProfileCompletionResponse completion
) {}
