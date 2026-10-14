package com.carhub.carrental.user.web.dto.client;

import java.time.LocalDate;
import com.carhub.carrental.user.persistence.entity.enums.IdentityDocumentType;

public record IdentityDocumentResponse(
        IdentityDocumentType type,
        String series,
        String number,
        LocalDate expiresAt
) {}
