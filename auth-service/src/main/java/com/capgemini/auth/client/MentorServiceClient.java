package com.capgemini.auth.client;

import com.capgemini.auth.dto.MentorSyncRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "mentor-service")
public interface MentorServiceClient {

    @PostMapping("/mentors/apply")
    @CircuitBreaker(name = "mentorService", fallbackMethod = "fallbackCreateMentor")
    void createMentor(@RequestHeader("Authorization") String authorization,
                      @RequestBody MentorSyncRequest request);

    default void fallbackCreateMentor(String authorization, MentorSyncRequest request, Throwable t) {
        // Log the failure and perhaps throw a more descriptive exception or ignore if non-critical
        // For registration sync, we might want to throw an exception to trigger a rollback in AuthService
        throw new RuntimeException("Mentor service is currently unavailable. Please try again later.", t);
    }
}
