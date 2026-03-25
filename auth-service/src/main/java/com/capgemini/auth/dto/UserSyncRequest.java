package com.capgemini.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSyncRequest {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String profileImage;
    private LocalDateTime createdAt;
}
