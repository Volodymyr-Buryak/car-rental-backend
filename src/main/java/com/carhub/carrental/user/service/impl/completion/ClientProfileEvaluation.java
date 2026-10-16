package com.carhub.carrental.user.service.impl.completion;

import lombok.*;
import java.util.Set;
import com.carhub.carrental.user.persistence.entity.ClientProfile;

public record ClientProfileEvaluation(
        @NonNull ClientProfile profile,
        @NonNull Set<ProfileSection> missingSections
) {
    public ClientProfileEvaluation {
        missingSections = Set.copyOf(missingSections);
    }
    public boolean isComplete() {
        return missingSections.isEmpty();
    }
}