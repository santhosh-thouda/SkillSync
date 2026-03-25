package com.capgemini.auth.client;

import com.capgemini.auth.dto.MentorSyncRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mentor-service")
public interface MentorServiceClient {

    @PostMapping("/mentors/apply")
    void createMentor(@RequestBody MentorSyncRequest request);
}
