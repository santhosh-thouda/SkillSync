package com.capgemini.group.service;

import com.capgemini.group.dto.GroupDto;
import com.capgemini.group.dto.GroupRequest;

import java.util.List;

public interface GroupService {
    GroupDto createGroup(GroupRequest request);
    GroupDto joinGroup(Long groupId, Long userId);
    GroupDto leaveGroup(Long groupId, Long userId);
    List<GroupDto> getAllGroups();
    GroupDto getGroupById(Long id);
    void deleteGroup(Long id);
}
