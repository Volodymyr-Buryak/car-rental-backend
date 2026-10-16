package com.carhub.carrental.user.web.dto.client.profile.section;

import java.util.Set;
import com.carhub.carrental.user.service.impl.completion.ProfileSection;

public record ProfileCompletionResponse(
        boolean complete,
        Set<ProfileSection> missingSections
) {}
