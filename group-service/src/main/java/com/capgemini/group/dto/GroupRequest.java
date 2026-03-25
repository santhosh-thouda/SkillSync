package com.capgemini.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GroupRequest {
    
    @NotBlank(message = "Group name is required")
    private String name;
    
    @NotBlank(message = "Group description is required")
    private String description;
    
    @NotNull(message = "CreatedBy user id is required")
    private Long createdBy;
}
