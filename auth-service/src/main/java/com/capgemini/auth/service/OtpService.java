package com.capgemini.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.capgemini.auth.repository.UserRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OtpService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${app.mail.from:santhoshthouda7576@gmail.com}")
    private String fromAddress;

    @Value("${app.mail.otp-expiry-minutes:10}")
    private int otpExpiryMinutes;

    private final Map<String, long[]> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public OtpService(JavaMailSender mailSender, UserRepository userRepository) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
    }

    public void generateAndSend(String email, String name) {
        // Check if email is already registered
        if (userRepository.findByEmail(email.toLowerCase()).isPresent()) {
            throw new RuntimeException("An account with this email already exists. Please sign in instead.");
        }

        String otp = String.format("%06d", random.nextInt(1_000_000));
        long expiry = Instant.now().getEpochSecond() + (otpExpiryMinutes * 60L);
        otpStore.put(email.toLowerCase(), new long[]{Long.parseLong(otp), expiry});

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(email);
            message.setSubject("SkillSync — Your verification code");
            message.setText(
                "Hi " + name + ",\n\n" +
                "Your SkillSync verification code is:\n\n" +
                "  " + otp + "\n\n" +
                "This code expires in " + otpExpiryMinutes + " minutes.\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "— The SkillSync Team"
            );
            mailSender.send(message);
            log.info("OTP sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage());
            // Remove from store so user can retry
            otpStore.remove(email.toLowerCase());
            throw new RuntimeException("Failed to send verification email. Please check your email address and try again.");
        }
    }

    public boolean verify(String email, String otp) {
        long[] stored = otpStore.get(email.toLowerCase());
        if (stored == null) return false;
        long storedOtp = stored[0];
        long expiry = stored[1];
        if (Instant.now().getEpochSecond() > expiry) {
            otpStore.remove(email.toLowerCase());
            return false;
        }
        boolean valid = String.valueOf(storedOtp).equals(otp.trim());
        if (valid) otpStore.remove(email.toLowerCase());
        return valid;
    }

    public boolean hasPendingOtp(String email) {
        long[] stored = otpStore.get(email.toLowerCase());
        if (stored == null) return false;
        if (Instant.now().getEpochSecond() > stored[1]) {
            otpStore.remove(email.toLowerCase());
            return false;
        }
        return true;
    }

    // Verified marker — stored separately so register can check
    private final Map<String, Boolean> verifiedEmails = new ConcurrentHashMap<>();

    public void markVerified(String email) {
        verifiedEmails.put(email.toLowerCase(), true);
    }

    public boolean isVerified(String email) {
        return Boolean.TRUE.equals(verifiedEmails.get(email.toLowerCase()));
    }

    public void clearVerified(String email) {
        verifiedEmails.remove(email.toLowerCase());
    }
}
