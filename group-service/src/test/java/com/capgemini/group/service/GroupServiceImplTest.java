package com.capgemini.group.service;

import com.capgemini.group.dto.GroupDto;
import com.capgemini.group.dto.GroupRequest;
import com.capgemini.group.entity.StudyGroup;
import com.capgemini.group.exception.ResourceNotFoundException;
import com.capgemini.group.mapper.GroupMapper;
import com.capgemini.group.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Spy
    private GroupMapper groupMapper;

    @InjectMocks
    private GroupServiceImpl groupService;

    @Test
    void createGroupShouldAddCreatorAsMember() {
        GroupRequest request = new GroupRequest();
        request.setName("Java Study Circle");
        request.setDescription("Spring Boot prep");
        request.setCreatedBy(101L);

        StudyGroup savedGroup = StudyGroup.builder()
                .id(1L)
                .name("Java Study Circle")
                .description("Spring Boot prep")
                .createdBy(101L)
                .members(new HashSet<>(Set.of(101L)))
                .build();

        when(groupRepository.save(any(StudyGroup.class))).thenReturn(savedGroup);

        GroupDto response = groupService.createGroup(request);

        assertEquals("Java Study Circle", response.getName());
        assertEquals(Set.of(101L), response.getMembers());
        verify(groupRepository).save(any(StudyGroup.class));
    }

    @Test
    void joinGroupShouldAddMember() {
        StudyGroup group = StudyGroup.builder()
                .id(1L)
                .name("Java Study Circle")
                .description("Spring Boot prep")
                .createdBy(101L)
                .members(new HashSet<>(Set.of(101L)))
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);

        GroupDto response = groupService.joinGroup(1L, 202L);

        assertEquals(Set.of(101L, 202L), response.getMembers());
    }

    @Test
    void leaveGroupShouldRemoveMember() {
        StudyGroup group = StudyGroup.builder()
                .id(1L)
                .name("Java Study Circle")
                .description("Spring Boot prep")
                .createdBy(101L)
                .members(new HashSet<>(Set.of(101L, 202L)))
                .build();

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(groupRepository.save(group)).thenReturn(group);

        GroupDto response = groupService.leaveGroup(1L, 202L);

        assertEquals(Set.of(101L), response.getMembers());
    }

    @Test
    void getGroupByIdShouldThrowWhenMissing() {
        when(groupRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.getGroupById(99L));
    }

    @Test
    void getAllGroupsShouldMapAllEntities() {
        StudyGroup first = StudyGroup.builder()
                .id(1L)
                .name("Java")
                .description("Core Java")
                .createdBy(1L)
                .members(new HashSet<>(Set.of(1L)))
                .build();
        StudyGroup second = StudyGroup.builder()
                .id(2L)
                .name("Spring")
                .description("Spring Boot")
                .createdBy(2L)
                .members(new HashSet<>(Set.of(2L)))
                .build();

        when(groupRepository.findAll()).thenReturn(List.of(first, second));

        List<GroupDto> response = groupService.getAllGroups();

        assertEquals(2, response.size());
        assertEquals("Java", response.get(0).getName());
        assertEquals("Spring", response.get(1).getName());
    }
}
