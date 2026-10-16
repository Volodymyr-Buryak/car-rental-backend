package com.carhub.carrental.user.web.dto.client.profile.section;

import java.time.LocalDate;
import java.util.stream.Stream;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class UpdatePersonalInfoRequestTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void closeValidatorFactory() {
        FACTORY.close();
    }

    @ParameterizedTest
    @MethodSource("nameBoundaries")
    void namesRespectTrimmedMinimumAndDatabaseCharacterLimit(String value, boolean valid) {
        for (String field : new String[]{"firstName", "lastName", "middleName"}) {
            assertThat(VALIDATOR.validateValue(UpdatePersonalInfoRequest.class, field, value).isEmpty())
                    .as("%s: %s", field, value)
                    .isEqualTo(valid);
        }
    }

    @Test
    void allFieldsAreRequired() {
        for (String field : new String[]{"firstName", "lastName", "middleName", "birthDate", "phoneNumber"}) {
            assertThat(VALIDATOR.validateValue(UpdatePersonalInfoRequest.class, field, null))
                    .as(field).isNotEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"+380000000000", "+380671234567", "+380999999999"})
    void acceptsPhoneNumbersWithinDatabaseBounds(String phone) {
        assertThat(VALIDATOR.validateValue(UpdatePersonalInfoRequest.class, "phoneNumber", phone)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "+12", "+12345", "+012345", "380671234567", "+123456789012345",
            "+38067123456", "+3806712345678", "+381671234567", "0671234567",
            "+380 671234567", "+380671234567\n", " +380671234567", "+380671234567 ",
            "+38067123456\u0667"})
    void rejectsPhoneNumbersOutsideDatabaseFormat(String phone) {
        assertThat(VALIDATOR.validateValue(UpdatePersonalInfoRequest.class, "phoneNumber", phone)).isNotEmpty();
    }

    @Test
    void birthDateMustBeInThePastWithoutAnInventedMinimumAge() {
        LocalDate today = LocalDate.now();
        assertThat(VALIDATOR.validateValue(UpdatePersonalInfoRequest.class, "birthDate", today.minusDays(1))).isEmpty();
        assertThat(VALIDATOR.validateValue(UpdatePersonalInfoRequest.class, "birthDate", today)).isNotEmpty();
        assertThat(VALIDATOR.validateValue(UpdatePersonalInfoRequest.class, "birthDate", today.plusDays(1))).isNotEmpty();
    }

    private static Stream<Arguments> nameBoundaries() {
        String supplementaryCharacter = "\uD801\uDC00";
        return Stream.of(
                Arguments.of("Олена", true),
                Arguments.of("  Олена  ", true),
                Arguments.of("", false),
                Arguments.of("  ", false),
                Arguments.of("А", false),
                Arguments.of(" А ", false),
                Arguments.of("А".repeat(150), true),
                Arguments.of("А".repeat(151), false),
                Arguments.of(" ".repeat(148) + "Ан", true),
                Arguments.of(" ".repeat(149) + "Ан", true),
                Arguments.of(supplementaryCharacter, true),
                Arguments.of(supplementaryCharacter.repeat(75), true),
                Arguments.of(supplementaryCharacter.repeat(76), false),
                Arguments.of(supplementaryCharacter.repeat(150), false),
                Arguments.of(supplementaryCharacter.repeat(151), false)
        );
    }
}
