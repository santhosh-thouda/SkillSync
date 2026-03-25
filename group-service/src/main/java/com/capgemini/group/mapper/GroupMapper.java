package com.capgemini.group.mapper;

import com.capgemini.group.dto.GroupDto;
import com.capgemini.group.entity.StudyGroup;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public GroupDto toDto(StudyGroup group) {
        return new GroupDto(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedBy(),
                group.getCreatedAt(),
                group.getMembers()
        );
    }
}
