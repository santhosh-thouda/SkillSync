package com.capgemini.auth.service;

import com.capgemini.auth.client.MentorServiceClient;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MentorServiceClient mentorServiceClient;
    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            MentorServiceClient mentorServiceClient,
            RestTemplate restTemplate,
            @Value("${user.service.url:http://localhost:8082}") String userServiceUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.mentorServiceClient = mentorServiceClient;
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    @PostConstruct
    public void initAdminUser() {
        if (userRepository.findByEmail("useradmin").isEmpty()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("useradmin");
            admin.setPassword(passwordEncoder.encode("useradmin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
        }
    }

    public void register(RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        try {
            syncRegistration(savedUser);
        } catch (RuntimeException ex) {
            userRepository.delete(savedUser);
            throw ex;
        }
    }

    private void syncRegistration(User savedUser) {
        String role = savedUser.getRole() == null ? "" : savedUser.getRole().trim().toUpperCase(Locale.ROOT);

        if ("LEARNER".equals(role) || "USER".equals(role)) {
            UserSyncRequest userRequest = new UserSyncRequest(
                    null,
                    savedUser.getName(),
                    savedUser.getEmail(),
                    "LEARNER",
                    null,
                    null);

            restTemplate.postForEntity(userServiceUrl + "/users", userRequest, String.class);
            return;
        }

        if ("MENTOR".equals(role)) {
            MentorSyncRequest mentorRequest = new MentorSyncRequest();
            mentorRequest.setUserId(savedUser.getId());
            mentorRequest.setBio("New mentor profile");
            mentorRequest.setExperience(0);
            mentorRequest.setHourlyRate(0.0);
            mentorRequest.setSkills(Collections.emptyList());
            mentorServiceClient.createMentor(mentorRequest);
        }
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse(token, user.getId(), user.getRole());
    }

    public AuthResponse refresh(RefreshRequest request) {
        String email = jwtUtil.extractEmail(request.getToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found from token"));

        String newToken = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(newToken, user.getId(), user.getRole());
    }
}
