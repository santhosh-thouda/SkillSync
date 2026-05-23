package com.capgemini.auth.service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * Core business logic service for Authentication and User Management.
 * Handles the registration workflow, BCrypt password hashing, JWT generation,
 * and orchestrates cross-service synchronization using Feign clients.
 */
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

    /**
     * Initializes a default administrator account upon application startup if one doesn't exist.
     * Also triggers a background synchronization for legacy mentor accounts.
     */
    @PostConstruct
    public void initAdminUser() {
        String email = "admin@skillsync.com";
        User admin = userRepository.findByEmail(email).orElse(new User());
        
        log.info("Initializing/Updating default admin user: {}", email);
        admin.setName("Admin User");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode("useradmin123"));
        admin.setRole("ROLE_ADMIN");
        
        userRepository.save(admin);
        
        // Sync existing mentors to ensure they have names in mentor-service
        syncAllMentors();
    }

    private void syncAllMentors() {
        log.info("Starting background sync for all existing mentors to fix missing names...");
        userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().toUpperCase().contains("MENTOR"))
                .forEach(user -> {
                    try {
                        syncRegistration(user);
                    } catch (Exception e) {
                        log.warn("Failed to sync mentor {}: {}", user.getEmail(), e.getMessage());
                    }
                });
    }

    /**
     * Registers a new user into the system.
     * Encrypts the raw password, saves to the local PostgreSQL database, and triggers
     * a synchronization event to ensure the profile is replicated in downstream services.
     *
     * @param request DTO containing the user's registration details.
     */
    @Transactional
    public void register(RegisterRequest request) {
        log.info("Registering new user with email: {} and role: {}", request.getEmail(), request.getRole());
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);
        syncRegistration(savedUser);
        log.info("Successfully registered and synced user: {}", savedUser.getEmail());
    }

    /**
     * Synchronizes the newly created user profile across bounded contexts (User Service and Mentor Service).
     * Prevents data silos by publishing the minimal required identity data.
     *
     * @param savedUser The newly persisted user entity.
     */
    private void syncRegistration(User savedUser) {
        String role = savedUser.getRole() == null ? "" : savedUser.getRole().trim().toUpperCase(Locale.ROOT);

        UserSyncRequest userRequest = new UserSyncRequest(
                null,
                savedUser.getName(),
                savedUser.getEmail(),
                normalizeRole(role),
                null,
                null);

        try {
            userServiceClient.createUser("Bearer " + jwtUtil.generateToken(
                    savedUser.getEmail(),
                    savedUser.getId(),
                    savedUser.getRole()), userRequest);
        } catch (Exception e) {
            log.error("Failed to sync user to user-service: {}", e.getMessage());
        }

        if (role.contains("MENTOR")) {
            log.info("Syncing mentor profile to mentor-service for email: {}", savedUser.getEmail());
            MentorSyncRequest mentorRequest = new MentorSyncRequest();
            mentorRequest.setUserId(savedUser.getId());
            mentorRequest.setName(savedUser.getName());
            // Other fields remain null to avoid overwriting existing data in mentor-service
            mentorRequest.setBio(null);
            mentorRequest.setExperience(null);
            mentorRequest.setHourlyRate(null);
            mentorRequest.setSkills(null);
            
            try {
                mentorServiceClient.createMentor("Bearer " + jwtUtil.generateToken(
                        savedUser.getEmail(),
                        savedUser.getId(),
                        savedUser.getRole()), mentorRequest);
            } catch (Exception e) {
                log.error("Failed to sync mentor to mentor-service: {}", e.getMessage());
            }
        }
    }

    private String normalizeRole(String role) {
        if (role.contains("ADMIN")) {
            return "ADMIN";
        }
        if (role.contains("MENTOR")) {
            return "MENTOR";
        }
        return "LEARNER";
    }

    /**
     * Authenticates a user by comparing the raw password against the stored BCrypt hash.
     *
     * @param request Login credentials containing email and plaintext password.
     * @return AuthResponse containing the minted JWT and role.
     * @throws ResourceNotFoundException if email does not exist.
     * @throws BadRequestException if password does not match.
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());

        return new AuthResponse(token, user.getId(), user.getRole());
    }

    /**
     * Refreshes an existing token, extending the user's session without re-authenticating.
     *
     * @param request Contains the current unexpired/expired valid JWT.
     * @return AuthResponse containing the new JWT.
     * @throws ResourceNotFoundException if the email within the token no longer maps to an active user.
     */
    public AuthResponse refresh(RefreshRequest request) {
        String email = jwtUtil.extractEmail(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found from token"));

        String newToken = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());
        return new AuthResponse(newToken, user.getId(), user.getRole());
    }
}
