package com.capgemini.auth.service;

import com.capgemini.auth.client.MentorServiceClient;
import com.capgemini.auth.client.UserServiceClient;
import com.capgemini.auth.dto.AuthResponse;
import com.capgemini.auth.dto.LoginRequest;
import com.capgemini.auth.dto.MentorSyncRequest;
import com.capgemini.auth.dto.RefreshRequest;
import com.capgemini.auth.dto.RegisterRequest;
import com.capgemini.auth.dto.UserSyncRequest;
import com.capgemini.auth.entity.User;
import com.capgemini.auth.exception.BadRequestException;
import com.capgemini.auth.exception.ResourceNotFoundException;
import com.capgemini.auth.repository.UserRepository;
import com.capgemini.auth.security.JwtUtil;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Locale;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MentorServiceClient mentorServiceClient;
    private final UserServiceClient userServiceClient;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            MentorServiceClient mentorServiceClient,
            UserServiceClient userServiceClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.mentorServiceClient = mentorServiceClient;
        this.userServiceClient = userServiceClient;
    }

    @PostConstruct
    public void initAdminUser() {
        if (userRepository.findByEmail("useradmin").isEmpty()) {
            log.info("Initializing default admin user: useradmin");
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("useradmin");
            admin.setPassword(passwordEncoder.encode("useradmin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }

    public void register(RegisterRequest request) {
        log.info("Registering new user with email: {} and role: {}", request.getEmail(), request.getRole());
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        try {
            syncRegistration(savedUser);
            log.info("Successfully registered and synced user: {}", savedUser.getEmail());
        } catch (RuntimeException ex) {
            log.error("Failed to sync registration for user: {}. Rolling back registration.", savedUser.getEmail(), ex);
            userRepository.delete(savedUser);
            throw ex;
        }
    }

    private void syncRegistration(User savedUser) {
        String role = savedUser.getRole() == null ? "" : savedUser.getRole().trim().toUpperCase(Locale.ROOT);

        if ("LEARNER".equals(role) || "USER".equals(role)) {
            log.info("Syncing learner profile to user-service for email: {}", savedUser.getEmail());
            UserSyncRequest userRequest = new UserSyncRequest(
                    null,
                    savedUser.getName(),
                    savedUser.getEmail(),
                    "LEARNER",
                    null,
                    null);

            userServiceClient.createUser("Bearer " + jwtUtil.generateToken(
                    savedUser.getEmail(),
                    savedUser.getId(),
                    savedUser.getRole()), userRequest);
            return;
        }

        if ("MENTOR".equals(role)) {
            log.info("Syncing mentor profile to mentor-service for email: {}", savedUser.getEmail());
            MentorSyncRequest mentorRequest = new MentorSyncRequest();
            mentorRequest.setUserId(savedUser.getId());
            mentorRequest.setBio("New mentor profile");
            mentorRequest.setExperience(0);
            mentorRequest.setHourlyRate(0.0);
            mentorRequest.setSkills(Collections.emptyList());
            mentorServiceClient.createMentor("Bearer " + jwtUtil.generateToken(
                    savedUser.getEmail(),
                    savedUser.getId(),
                    savedUser.getRole()), mentorRequest);
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());

        return new AuthResponse(token, user.getId(), user.getRole());
    }

    public AuthResponse refresh(RefreshRequest request) {
        String email = jwtUtil.extractEmail(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found from token"));

        String newToken = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
        return new AuthResponse(newToken, user.getId(), user.getRole());
    }

    // Helper removed as Feign clients handle headers directly
}
