package com.capgemini.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserUpdateRequest {
    
    @NotBlank(message = "Name cannot be empty")
    private String name;
    
    private String profileImage;
}
