package com.capgemini.mentor.mapper;

import com.capgemini.mentor.dto.MentorDto;
import com.capgemini.mentor.entity.Mentor;
import org.springframework.stereotype.Component;

@Component
public class MentorMapper {

    public MentorDto toDto(com.capgemini.mentor.entity.Mentor mentor) {
        return new MentorDto(
                mentor.getId(),
                mentor.getUserId(),
                mentor.getName(),
                mentor.getEarnings(),
                mentor.getBio(),
                mentor.getExperience(),
                mentor.getRating(),
                mentor.getHourlyRate(),
                mentor.isAvailable(),
                mentor.getApproved(),
                mentor.getSkills(),
                mentor.getCreatedAt()
        );
    }
}
