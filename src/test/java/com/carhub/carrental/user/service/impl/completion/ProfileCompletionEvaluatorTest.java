package com.carhub.carrental.user.service.impl.completion;

import java.time.LocalDate;
import com.carhub.carrental.user.persistence.entity.ClientProfile;
import com.carhub.carrental.user.persistence.entity.User;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileCompletionEvaluatorTest {
    private final ProfileCompletionEvaluator evaluator = new ProfileCompletionEvaluator();

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void personalInfoIsIncompleteWithoutMiddleName(String middleName) {
        ClientProfile profile = profileWithMiddleName(middleName);

        assertThat(evaluator.evaluate(profile).missingSections()).contains(ProfileSection.PERSONAL_INFO);
    }

    @Test
    void personalInfoIsCompleteWithAllRequiredFields() {
        ClientProfile profile = profileWithMiddleName("Ivanivna");

        assertThat(evaluator.evaluate(profile).missingSections()).doesNotContain(ProfileSection.PERSONAL_INFO);
    }

    private ClientProfile profileWithMiddleName(String middleName) {
        User user = new User();
        user.setFirstName("Olena");
        user.setLastName("Koval");
        user.setMiddleName(middleName);
        user.setPhoneNumber("+380501234567");
        return ClientProfile.builder().user(user).birthDate(LocalDate.of(1995, 5, 20)).build();
    }
}
