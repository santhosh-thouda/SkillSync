package com.capgemini.auth.controller;

import com.capgemini.auth.dto.*;
import com.capgemini.auth.service.AuthService;
import com.capgemini.auth.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(AuthService authService, OtpService otpService) {
        this.authService = authService;
        this.otpService = otpService;
    }

    /**
     * Step 1: Send OTP to email before registration
     */
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        otpService.generateAndSend(request.getEmail(), request.getName());
        return ResponseEntity.ok(Map.of("message", "Verification code sent to " + request.getEmail()));
    }

    /**
     * Step 2: Verify OTP (optional standalone check — frontend can call this)
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        boolean valid = otpService.verify(request.getEmail(), request.getOtp());
        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid or expired verification code."));
        }
        // Re-store a "verified" marker so register can proceed
        // We use a special sentinel OTP "VERIFIED" to mark the email as verified
        otpService.markVerified(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
    }

    /**
     * Step 3: Register — only allowed after OTP verification
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        if (!otpService.isVerified(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email not verified. Please complete OTP verification first."));
        }
        authService.register(request);
        otpService.clearVerified(request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}
