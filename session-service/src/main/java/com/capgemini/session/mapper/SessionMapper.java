package com.capgemini.session.mapper;

import com.capgemini.session.dto.SessionDto;
import com.capgemini.session.entity.MentorshipSession;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public SessionDto toDto(MentorshipSession session) {
        return new SessionDto(
                session.getId(),
                session.getMentorId(),
                session.getLearnerId(),
                session.getSessionDate(),
                session.getStatus(),
                session.getCreatedAt()
        );
    }
}
