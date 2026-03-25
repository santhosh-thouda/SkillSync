package com.capgemini.group.service;

import com.capgemini.group.dto.GroupDto;
import com.capgemini.group.dto.GroupRequest;
import com.capgemini.group.entity.StudyGroup;
import com.capgemini.group.exception.ResourceNotFoundException;
import com.capgemini.group.mapper.GroupMapper;
import com.capgemini.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    @Override
    public GroupDto createGroup(GroupRequest request) {
        StudyGroup group = StudyGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(request.getCreatedBy())
                .build();
        
        group.getMembers().add(request.getCreatedBy());
        
        StudyGroup savedGroup = groupRepository.save(group);
        return groupMapper.toDto(savedGroup);
    }

    @Override
    public GroupDto joinGroup(Long groupId, Long userId) {
        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
                
        group.getMembers().add(userId);
        StudyGroup updatedGroup = groupRepository.save(group);
        return groupMapper.toDto(updatedGroup);
    }

    @Override
    public GroupDto leaveGroup(Long groupId, Long userId) {
        StudyGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
                
        group.getMembers().remove(userId);
        StudyGroup updatedGroup = groupRepository.save(group);
        return groupMapper.toDto(updatedGroup);
    }

    @Override
    public List<GroupDto> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(groupMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public GroupDto getGroupById(Long id) {
        StudyGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
        return groupMapper.toDto(group);
    }

    @Override
    public void deleteGroup(Long id) {
        StudyGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
        groupRepository.delete(group);
    }
}
