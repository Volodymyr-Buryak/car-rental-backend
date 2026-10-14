package com.carhub.carrental.user.web.dto.client;

import java.time.LocalDate;

public record DriverLicenseResponse(
        String series,
        String number,
        LocalDate issuedAt,
        LocalDate expiresAt,
        LocalDate categoryBOpenedAt
) {}
