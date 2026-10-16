package com.carhub.carrental.user.web.dto.client.profile.section;

import java.time.LocalDate;

public record DriverLicenseResponse(
        String maskedNumber,
        LocalDate categoryBOpenedAt,
        String verificationStatus
) {}