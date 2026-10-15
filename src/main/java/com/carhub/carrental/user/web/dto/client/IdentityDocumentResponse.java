package com.carhub.carrental.user.web.dto.client;

import com.carhub.carrental.user.persistence.entity.enums.IdentityDocumentType;

public record IdentityDocumentResponse(
        IdentityDocumentType type,
        String maskedNumber,
        String verificationStatus
) {}
