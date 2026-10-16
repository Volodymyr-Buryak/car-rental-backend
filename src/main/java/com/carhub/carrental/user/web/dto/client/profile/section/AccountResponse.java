package com.carhub.carrental.user.web.dto.client.profile.section;

public record AccountResponse(
        String email,
        String avatarUrl,
        boolean emailVerified
) {}