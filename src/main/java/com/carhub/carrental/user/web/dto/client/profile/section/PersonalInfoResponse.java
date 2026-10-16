package com.carhub.carrental.user.web.dto.client.profile.section;

import java.time.LocalDate;

public record PersonalInfoResponse(
        String firstName,
        String lastName,
        String middleName,
        LocalDate birthDate,
        String phoneNumber
) {}
