package com.capgemini.auth.service;

import com.capgemini.auth.client.MentorServiceClient;
import com.capgemini.auth.dto.AuthResponse;
import com.capgemini.auth.dto.LoginRequest;
import com.capgemini.auth.dto.MentorSyncRequest;
import com.capgemini.auth.dto.RefreshRequest;
import com.capgemini.auth.dto.RegisterRequest;
import com.capgemini.auth.dto.UserSyncRequest;
import com.capgemini.auth.entity.User;
import com.capgemini.auth.repository.UserRepository;
import com.capgemini.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MentorServiceClient mentorServiceClient;

    @Mock
    private RestTemplate restTemplate;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtUtil,
                mentorServiceClient,
                restTemplate,
                "http://localhost:8082");
    }

    @Test
    void registerLearnerShouldSaveAndSyncUserProfile() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alex");
        request.setEmail("alex@example.com");
        request.setPassword("secret");
        request.setRole("learner");

        User savedUser = new User(1L, "Alex", "alex@example.com", "encoded-secret", "learner");

        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(restTemplate.postForEntity(
                eq("http://localhost:8082/users"),
                any(UserSyncRequest.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok("created"));

        authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("Alex", userCaptor.getValue().getName());
        assertEquals("alex@example.com", userCaptor.getValue().getEmail());
        assertEquals("encoded-secret", userCaptor.getValue().getPassword());

        ArgumentCaptor<UserSyncRequest> syncCaptor = ArgumentCaptor.forClass(UserSyncRequest.class);
        verify(restTemplate).postForEntity(eq("http://localhost:8082/users"), syncCaptor.capture(), eq(String.class));
        assertEquals("Alex", syncCaptor.getValue().getName());
        assertEquals("alex@example.com", syncCaptor.getValue().getEmail());
        assertEquals("LEARNER", syncCaptor.getValue().getRole());
        verify(mentorServiceClient, never()).createMentor(any(MentorSyncRequest.class));
    }

    @Test
    void registerMentorShouldRollbackWhenSyncFails() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Taylor");
        request.setEmail("taylor@example.com");
        request.setPassword("secret");
        request.setRole("MENTOR");

        User savedUser = new User(5L, "Taylor", "taylor@example.com", "encoded-secret", "MENTOR");

        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        doThrow(new RuntimeException("mentor sync failed"))
                .when(mentorServiceClient).createMentor(any(MentorSyncRequest.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));

        assertEquals("mentor sync failed", exception.getMessage());
        verify(userRepository).delete(savedUser);
        verify(restTemplate, never()).postForEntity(any(String.class), any(), eq(String.class));
    }

    @Test
    void loginShouldReturnJwtWhenCredentialsMatch() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alex@example.com");
        request.setPassword("secret");

        User user = new User(10L, "Alex", "alex@example.com", "hashed", "LEARNER");

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("alex@example.com")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals(10L, response.getUserId());
        assertEquals("LEARNER", response.getRole());
    }

    @Test
    void loginShouldFailWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alex@example.com");
        request.setPassword("wrong-password");

        User user = new User(10L, "Alex", "alex@example.com", "hashed", "LEARNER");

        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));

        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    void refreshShouldCreateNewTokenForExistingUser() {
        RefreshRequest request = new RefreshRequest("old-token");
        User user = new User(7L, "Alex", "alex@example.com", "hashed", "ADMIN");

        when(jwtUtil.extractEmail("old-token")).thenReturn("alex@example.com");
        when(userRepository.findByEmail("alex@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("alex@example.com")).thenReturn("new-token");

        AuthResponse response = authService.refresh(request);

        assertEquals("new-token", response.getToken());
        assertEquals(7L, response.getUserId());
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void initAdminUserShouldCreateDefaultAdminWhenMissing() {
        when(userRepository.findByEmail("useradmin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("useradmin123")).thenReturn("encoded-admin");

        authService.initAdminUser();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("Admin User", userCaptor.getValue().getName());
        assertEquals("useradmin", userCaptor.getValue().getEmail());
        assertEquals("encoded-admin", userCaptor.getValue().getPassword());
        assertEquals("ADMIN", userCaptor.getValue().getRole());
    }

    @Test
    void initAdminUserShouldSkipCreationWhenAdminAlreadyExists() {
        when(userRepository.findByEmail("useradmin")).thenReturn(Optional.of(new User()));

        authService.initAdminUser();

        verify(userRepository, never()).save(any(User.class));
    }
}
