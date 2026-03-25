package com.capgemini.skill.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkillRequest {
    
    @NotBlank(message = "Skill name is required")
    private String name;
    
    @NotBlank(message = "Skill category is required")
    private String category;
}
