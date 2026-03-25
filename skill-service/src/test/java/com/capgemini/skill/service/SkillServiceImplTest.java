package com.capgemini.skill.service;

import com.capgemini.skill.dto.SkillDto;
import com.capgemini.skill.dto.SkillRequest;
import com.capgemini.skill.entity.Skill;
import com.capgemini.skill.exception.ResourceNotFoundException;
import com.capgemini.skill.mapper.SkillMapper;
import com.capgemini.skill.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillServiceImplTest {

    @Mock
    private SkillRepository skillRepository;

    @Spy
    private SkillMapper skillMapper;

    @InjectMocks
    private SkillServiceImpl skillService;

    @Test
    void createSkillShouldReturnExistingSkillWhenNameAlreadyExists() {
        SkillRequest request = new SkillRequest();
        request.setName("Java");
        request.setCategory("Programming");

        Skill existing = Skill.builder()
                .id(1L)
                .name("Java")
                .category("Programming")
                .build();

        when(skillRepository.findByNameIgnoreCase("Java")).thenReturn(Optional.of(existing));

        SkillDto response = skillService.createSkill(request);

        assertEquals(1L, response.getId());
        assertEquals("Java", response.getName());
        verify(skillRepository, never()).save(any(Skill.class));
    }

    @Test
    void createSkillShouldPersistWhenMissing() {
        SkillRequest request = new SkillRequest();
        request.setName("Docker");
        request.setCategory("DevOps");

        Skill saved = Skill.builder()
                .id(2L)
                .name("Docker")
                .category("DevOps")
                .build();

        when(skillRepository.findByNameIgnoreCase("Docker")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenReturn(saved);

        SkillDto response = skillService.createSkill(request);

        assertEquals(2L, response.getId());
        assertEquals("Docker", response.getName());
        assertEquals("DevOps", response.getCategory());
    }

    @Test
    void getSkillByIdShouldThrowWhenMissing() {
        when(skillRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> skillService.getSkillById(10L));
    }

    @Test
    void getAllSkillsShouldMapEntities() {
        when(skillRepository.findAll()).thenReturn(List.of(
                Skill.builder().id(1L).name("Java").category("Programming").build(),
                Skill.builder().id(2L).name("Spring").category("Framework").build()
        ));

        List<SkillDto> response = skillService.getAllSkills();

        assertEquals(2, response.size());
        assertEquals("Java", response.get(0).getName());
        assertEquals("Spring", response.get(1).getName());
    }
}
