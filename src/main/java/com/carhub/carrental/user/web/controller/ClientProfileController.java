package com.carhub.carrental.user.web.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import com.carhub.carrental.user.service.ClientProfileService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.carhub.carrental.user.web.dto.client.ClientProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clients/me/profile")
public class ClientProfileController {

    private final ClientProfileService clientProfileService;

    @GetMapping
    public ClientProfileResponse getProfile(@AuthenticationPrincipal Jwt jwt) {
        return clientProfileService.getProfile(UUID.fromString(jwt.getSubject()));
    }

}
