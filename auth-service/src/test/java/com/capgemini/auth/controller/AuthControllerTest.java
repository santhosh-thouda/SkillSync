package com.capgemini.auth.controller;

import com.capgemini.auth.dto.AuthResponse;
import com.capgemini.auth.dto.LoginRequest;
import com.capgemini.auth.dto.RefreshRequest;
import com.capgemini.auth.dto.RegisterRequest;
import com.capgemini.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerShouldDelegateToService() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("alex@example.com");

        ResponseEntity<String> response = authController.register(request);

        verify(authService).register(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    void loginShouldReturnServiceResponse() {
        LoginRequest request = new LoginRequest();
        AuthResponse expected = new AuthResponse("jwt-token", 1L, "LEARNER");
        when(authService.login(request)).thenReturn(expected);

        ResponseEntity<AuthResponse> response = authController.login(request);

        verify(authService).login(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void refreshShouldReturnServiceResponse() {
        RefreshRequest request = new RefreshRequest("old-token");
        AuthResponse expected = new AuthResponse("new-token", 1L, "LEARNER");
        when(authService.refresh(request)).thenReturn(expected);

        ResponseEntity<AuthResponse> response = authController.refresh(request);

        verify(authService).refresh(request);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }
}
