package com.carhub.carrental.user.persistence.mapper;

import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import com.carhub.carrental.user.persistence.entity.ClientProfile;
import com.carhub.carrental.user.persistence.entity.DriverLicense;
import com.carhub.carrental.user.persistence.entity.IdentityDocument;
import com.carhub.carrental.user.persistence.entity.User;
import com.carhub.carrental.user.persistence.entity.enums.IdentityDocumentType;
import com.carhub.carrental.user.service.impl.completion.ClientProfileEvaluation;
import com.carhub.carrental.user.service.impl.completion.ProfileSection;
import com.carhub.carrental.user.web.dto.client.profile.ClientProfileResponse;
import com.carhub.carrental.user.web.dto.client.profile.section.*;

import static org.assertj.core.api.Assertions.assertThat;

class ClientProfileMapperTest {

    private final ClientProfileMapper mapper = Mappers.getMapper(ClientProfileMapper.class);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mapper, "userMapper", Mappers.getMapper(UserMapper.class));
    }

    @Test
    void mapsProfileAndEvaluationIntoNestedResponse() {
        var birthDate = LocalDate.of(1995, 3, 12);
        var issuedAt = LocalDate.of(2020, 1, 1);
        var expiresAt = LocalDate.of(2030, 1, 1);
        var categoryOpenedAt = LocalDate.of(2015, 1, 1);
        var user = new User();
        user.setFirstName("Ivan");
        user.setLastName("Petrenko");
        user.setMiddleName("Petrovych");
        user.setPhoneNumber("+380671234567");
        user.setEmail("ivan@example.com");
        user.setAvatarUrl("/avatar.png");
        var profile = ClientProfile.builder().user(user).birthDate(birthDate).build();
        profile.addIdentityDocument(IdentityDocument.builder().type(IdentityDocumentType.ID_CARD)
                .number("123456789").build());
        profile.addDriverLicense(DriverLicense.builder().series("ABC").number("123456")
                .issuedAt(issuedAt).expiresAt(expiresAt).categoryBOpenedAt(categoryOpenedAt).build());

        var response = mapper.toResponse(new ClientProfileEvaluation(profile, Set.of()));

        assertThat(response).isEqualTo(new ClientProfileResponse(
                new PersonalInfoResponse("Ivan", "Petrenko", "Petrovych", birthDate, "+380671234567"),
                new AccountResponse("ivan@example.com", "/avatar.png", false),
                new IdentityDocumentResponse(IdentityDocumentType.ID_CARD, "6789", null),
                new DriverLicenseResponse("3456", categoryOpenedAt, null),
                new ProfileCompletionResponse(true, Set.of())
        ));
    }

    @Test
    void keepsAbsentDocumentsNullAndUsesEvaluatorSectionResults() {
        var profile = ClientProfile.builder().user(new User()).build();
        var response = mapper.toResponse(new ClientProfileEvaluation(profile,
                Set.of(ProfileSection.PERSONAL_INFO, ProfileSection.DRIVER_LICENSE)));

        assertThat(response.identityDocument()).isNull();
        assertThat(response.driverLicense()).isNull();
        assertThat(response.completion()).isEqualTo(new ProfileCompletionResponse(false,
                Set.of(ProfileSection.PERSONAL_INFO, ProfileSection.DRIVER_LICENSE)));
    }

    @Test
    void returnsLastFourCharactersAndHandlesMissingValues() {
        assertThat(mapper.maskDocumentNumber("1234")).isEqualTo("1234");
        assertThat(mapper.maskDocumentNumber("1")).isNull();
        assertThat(mapper.maskDocumentNumber("")).isNull();
        assertThat(mapper.maskDocumentNumber(null)).isNull();
        assertThat(mapper.toResponse(null)).isNull();
    }
}
