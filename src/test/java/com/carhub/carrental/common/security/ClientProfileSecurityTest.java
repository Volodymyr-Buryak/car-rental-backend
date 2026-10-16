package com.carhub.carrental.common.security;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import com.carhub.carrental.user.service.ClientProfileService;
import com.carhub.carrental.user.web.controller.ClientProfileController;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = ClientProfileSecurityTest.TestConfiguration.class)
class ClientProfileSecurityTest {

    private static final String PROFILE_URL = "/api/v1/clients/me/profile";

    @Autowired private WebApplicationContext context;
    @Autowired private ClientProfileService profileService;
    @Autowired private JwtDecoder jwtDecoder;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        reset(profileService, jwtDecoder);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void anonymousRequestIsRejectedBeforeCallingService() throws Exception {
        mvc.perform(get(PROFILE_URL)).andExpect(status().isUnauthorized());
        verifyNoInteractions(profileService);
    }

    @Test
    void invalidBearerTokenIsRejected() throws Exception {
        when(jwtDecoder.decode("invalid")).thenThrow(new BadJwtException("Invalid token"));
        mvc.perform(get(PROFILE_URL).header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(profileService);
    }

    @Test
    void authenticatedRequestUsesIdentityFromJwtInsteadOfRequestParameter() throws Exception {
        var identityId = UUID.randomUUID();
        mvc.perform(get(PROFILE_URL)
                        .param("identityId", UUID.randomUUID().toString())
                        .with(jwt().jwt(token -> token.subject(identityId.toString()))))
                .andExpect(status().isOk());
        verify(profileService).getClientProfile(identityId);
        verifyNoMoreInteractions(profileService);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebMvc
    @Import({SecurityConfig.class, ClientProfileController.class})
    static class TestConfiguration {
        @Bean
        ClientProfileService profileService() {
            return mock(ClientProfileService.class);
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return mock(JwtDecoder.class);
        }
    }
}
