package com.capgemini.session.client;

import com.capgemini.session.dto.MentorDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "mentor-service")
public interface MentorServiceClient {

    @GetMapping("/mentors/{id}")
    MentorDto getMentorById(@RequestHeader("Authorization") String authorization, @PathVariable Long id);

    @PutMapping("/mentors/{id}/earnings")
    void addEarnings(@RequestHeader("Authorization") String authorization, 
                     @PathVariable Long id, 
                     @RequestParam("amount") Double amount);
}
