package com.capgemini.skill.service;

import com.capgemini.skill.dto.SkillDto;
import com.capgemini.skill.dto.SkillRequest;
import com.capgemini.skill.entity.Skill;
import com.capgemini.skill.exception.ResourceNotFoundException;
import com.capgemini.skill.mapper.SkillMapper;
import com.capgemini.skill.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public SkillDto createSkill(SkillRequest request) {
        Optional<Skill> existingSkill = skillRepository.findByNameIgnoreCase(request.getName());
        if (existingSkill.isPresent()) {
            return skillMapper.toDto(existingSkill.get());
        }

        Skill skill = Skill.builder()
                .name(request.getName())
                .category(request.getCategory())
                .build();
                
        Skill savedSkill = skillRepository.save(skill);
        return skillMapper.toDto(savedSkill);
    }

    @Override
    public List<SkillDto> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(skillMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SkillDto getSkillById(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
        return skillMapper.toDto(skill);
    }

    @Override
    public void deleteSkill(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));
        skillRepository.delete(skill);
    }
}
