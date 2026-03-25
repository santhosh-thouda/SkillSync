package com.capgemini.skill.mapper;

import com.capgemini.skill.dto.SkillDto;
import com.capgemini.skill.entity.Skill;
import org.springframework.stereotype.Component;

@Component
public class SkillMapper {

    public SkillDto toDto(Skill skill) {
        return new SkillDto(
                skill.getId(),
                skill.getName(),
                skill.getCategory()
        );
    }
}
