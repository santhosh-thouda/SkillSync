package com.capgemini.skill.service;

import com.capgemini.skill.dto.SkillDto;
import com.capgemini.skill.dto.SkillRequest;

import java.util.List;

public interface SkillService {
    SkillDto createSkill(SkillRequest request);
    List<SkillDto> getAllSkills();
    SkillDto getSkillById(Long id);
    void deleteSkill(Long id);
}
