package com.carhub.carrental.user.web.dto.client;

public record ProfileCompletionResponse(
        boolean complete,
        ProfileCompletionSectionsResponse sections
) {}
