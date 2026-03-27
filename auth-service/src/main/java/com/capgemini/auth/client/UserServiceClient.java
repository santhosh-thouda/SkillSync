package com.capgemini.auth.client;

import com.capgemini.auth.dto.UserSyncRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PostMapping("/users")
    void createUser(@RequestHeader("Authorization") String token, @RequestBody UserSyncRequest request);
}
