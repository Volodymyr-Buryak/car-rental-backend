package com.carhub.carrental.user.web.controller;

import java.util.UUID;
import java.util.Objects;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;
import com.carhub.carrental.user.service.ClientProfileService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.carhub.carrental.user.web.dto.client.profile.ClientProfileResponse;
import com.carhub.carrental.user.web.dto.client.profile.section.UpdatePersonalInfoRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clients/me")
public class ClientProfileController {

    private final ClientProfileService clientProfileService;

    @GetMapping("/profile")
    public ClientProfileResponse getClientProfile(@AuthenticationPrincipal Jwt jwt) {
        return clientProfileService.getClientProfile(
                UUID.fromString(Objects.requireNonNull(jwt.getSubject()))
        );
    }

    @PutMapping("/personal-info")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePersonalInfo(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdatePersonalInfoRequest request
    ) {
        clientProfileService.updatePersonalInfo(
                UUID.fromString(Objects.requireNonNull(jwt.getSubject())),
                request
        );
    }


}
