package com.carhub.carrental.user.web.dto.client.profile;

import com.carhub.carrental.user.web.dto.client.profile.section.*;

public record ClientProfileResponse(
        PersonalInfoResponse personalInfo,
        AccountResponse account,
        IdentityDocumentResponse identityDocument,
        DriverLicenseResponse driverLicense,
        ProfileCompletionResponse completion
) {}
