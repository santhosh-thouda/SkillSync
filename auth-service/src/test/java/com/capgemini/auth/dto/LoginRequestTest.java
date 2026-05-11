package com.capgemini.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void loginAllowsExistingPasswordThatDoesNotMatchRegistrationComplexity() {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@skillsync.com");
        request.setPassword("useradmin123");

        assertTrue(validator.validate(request).isEmpty());
    }
}
