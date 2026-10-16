package com.carhub.carrental.user.persistence.mapper;

import org.mapstruct.*;
import com.carhub.carrental.user.web.dto.client.profile.section.*;
import com.carhub.carrental.user.persistence.entity.DriverLicense;
import com.carhub.carrental.user.persistence.entity.ClientProfile;
import com.carhub.carrental.user.persistence.entity.IdentityDocument;
import com.carhub.carrental.user.web.dto.client.profile.ClientProfileResponse;
import com.carhub.carrental.user.service.impl.completion.ClientProfileEvaluation;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ClientProfileMapper {

    @Mapping(target = "personalInfo", source = "profile")
    @Mapping(target = "account", source = "profile.user")
    @Mapping(target = "identityDocument", source = "profile.identityDocument")
    @Mapping(target = "driverLicense", source = "profile.driverLicense")
    @Mapping(target = "completion", source = "evaluation")
    ClientProfileResponse toResponse(ClientProfileEvaluation evaluation);

    @Mapping(target = ".", source = "user")
    @Mapping(target = "birthDate", source = "birthDate")
    PersonalInfoResponse toPersonalInfoResponse(ClientProfile profile);

    @Mapping(target = "complete", expression = "java(evaluation.isComplete())")
    ProfileCompletionResponse toProfileCompletionResponse(ClientProfileEvaluation evaluation);

    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "maskedNumber", source = "number", qualifiedByName = "maskDocumentNumber")
    IdentityDocumentResponse toIdentityDocumentResponse(IdentityDocument document);

    @Mapping(target = "verificationStatus", ignore = true)
    @Mapping(target = "maskedNumber", source = "number", qualifiedByName = "maskDocumentNumber")
    DriverLicenseResponse toDriverLicenseResponse(DriverLicense license);

    @Named("maskDocumentNumber")
    default String maskDocumentNumber(String number) {
        if (number == null || number.length() < 4) {
            return null;
        }
        return number.substring(number.length() - 4);
    }
}