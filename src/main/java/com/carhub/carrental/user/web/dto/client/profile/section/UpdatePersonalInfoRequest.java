package com.carhub.carrental.user.web.dto.client.profile.section;

import java.time.LocalDate;
import jakarta.validation.constraints.*;
import com.carhub.carrental.user.web.validation.ValidPersonName;

public record UpdatePersonalInfoRequest(
        @NotBlank
        @ValidPersonName
        String firstName,

        @NotBlank
        @ValidPersonName
        String lastName,

        @NotBlank
        @ValidPersonName
        String middleName,

        @Past
        @NotNull
        LocalDate birthDate,

        @NotBlank
        @Pattern(regexp = "^[+]380[0-9]{9}$")
        String phoneNumber
) {}
