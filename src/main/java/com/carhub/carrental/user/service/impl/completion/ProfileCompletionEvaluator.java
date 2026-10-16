package com.carhub.carrental.user.service.impl.completion;

import java.util.Set;
import java.util.EnumSet;
import org.springframework.stereotype.Component;
import org.flywaydb.core.internal.util.StringUtils;
import com.carhub.carrental.user.persistence.entity.User;
import com.carhub.carrental.user.persistence.entity.ClientProfile;

@Component
public class ProfileCompletionEvaluator {

    public ClientProfileEvaluation evaluate(ClientProfile profile) {
        Set<ProfileSection> missing = EnumSet.noneOf(ProfileSection.class);

        if (!isPersonalInfoComplete(profile)) {
            missing.add(ProfileSection.PERSONAL_INFO);
        }

        if (profile.getIdentityDocument() == null) {
            missing.add(ProfileSection.IDENTITY_DOCUMENT);
        }

        if (profile.getDriverLicense() == null) {
            missing.add(ProfileSection.DRIVER_LICENSE);
        }

        return new ClientProfileEvaluation(profile, missing);
    }

    private boolean isPersonalInfoComplete(ClientProfile profile) {
        User user = profile.getUser();
        return StringUtils.hasText(user.getFirstName())
                && StringUtils.hasText(user.getLastName())
                && StringUtils.hasText(user.getMiddleName())
                && StringUtils.hasText(user.getPhoneNumber())
                && profile.getBirthDate() != null;
    }
}
