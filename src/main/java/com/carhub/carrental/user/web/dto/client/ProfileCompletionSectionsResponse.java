package com.carhub.carrental.user.web.dto.client;

public record ProfileCompletionSectionsResponse(
        boolean personalInfo,
        boolean identityDocument,
        boolean driverLicense
) {}
