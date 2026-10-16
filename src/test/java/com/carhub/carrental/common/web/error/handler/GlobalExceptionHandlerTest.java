package com.carhub.carrental.common.web.error.handler;

import java.util.UUID;
import java.util.stream.Stream;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.carhub.carrental.common.exception.ApplicationException;
import com.carhub.carrental.common.exception.NotFoundException;
import com.carhub.carrental.common.web.error.model.ApiProblemFactory;
import com.carhub.carrental.user.service.exception.profile.ClientProfileNotFoundException;
import com.carhub.carrental.user.service.exception.user.UserNotFoundException;
import com.carhub.carrental.user.service.exception.user.IdentityEmailMismatchException;
import com.carhub.carrental.user.integration.keycloak.exception.IdentityProviderException;
import com.carhub.carrental.user.integration.keycloak.exception.IdentityProviderUnavailableException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final ErrorController controller = new ErrorController();
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var messages = new ResourceBundleMessageSource();
        messages.setBasename("messages");
        var factory = new ApiProblemFactory(messages);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(factory),
                        new CustomResponseEntityExceptionHandler(factory))
                .build();
    }

    @ParameterizedTest
    @MethodSource("notFoundExceptions")
    void domainNotFoundExceptionsReturn404(NotFoundException exception, String detail) throws Exception {
        controller.failure = exception;

        mvc.perform(get("/test/errors"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value(detail))
                .andExpect(jsonPath("$.instance").value("/test/errors"));
    }

    @Test
    void unexpectedFailureReturns500WithoutInternalMessage() throws Exception {
        controller.failure = new IllegalStateException("Database credentials: secret");

        expectInternalServerError();
    }

    @ParameterizedTest
    @MethodSource("failuresWithoutHttpMapping")
    void failuresWithoutHttpMappingAreNotTreatedAsMissingResources(RuntimeException exception) throws Exception {
        controller.failure = exception;

        expectInternalServerError();
    }

    @Test
    void malformedJsonIsHandledByMvcAdviceInsteadOfCatchAll() throws Exception {
        mvc.perform(post("/test/errors").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void validationStillReturnsFieldErrors() throws Exception {
        mvc.perform(post("/test/errors").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].pointer").value("/name"))
                .andExpect(jsonPath("$.errors[0].detail").value("Name is required"));
    }

    private void expectInternalServerError() throws Exception {
        mvc.perform(get("/test/errors"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred."));
    }

    private static Stream<Arguments> notFoundExceptions() {
        var identityId = UUID.randomUUID();
        return Stream.of(
                Arguments.of(new UserNotFoundException(identityId), "User was not found."),
                Arguments.of(new ClientProfileNotFoundException(identityId), "Client profile was not found.")
        );
    }

    private static Stream<RuntimeException> failuresWithoutHttpMapping() {
        return Stream.of(
                new ApplicationException("error.internal", "Internal failure") {},
                new IdentityEmailMismatchException(UUID.randomUUID()),
                new IdentityProviderException("External user not found", 404, new RuntimeException()),
                new IdentityProviderUnavailableException("Keycloak unavailable", 503, new RuntimeException())
        );
    }

    @RestController
    static class ErrorController {
        private RuntimeException failure;

        @GetMapping("/test/errors")
        String fail() {
            throw failure;
        }

        @PostMapping("/test/errors")
        void validate(@Valid @RequestBody ValidatedRequest request) {}
    }

    record ValidatedRequest(@NotBlank(message = "Name is required") String name) {}
}
