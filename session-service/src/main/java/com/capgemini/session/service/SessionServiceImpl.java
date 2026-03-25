package com.capgemini.session.service;

import com.capgemini.session.dto.SessionDto;
import com.capgemini.session.dto.SessionRequest;
import com.capgemini.session.entity.MentorshipSession;
import com.capgemini.session.event.SessionEvent;
import com.capgemini.session.exception.ResourceNotFoundException;
import com.capgemini.session.mapper.SessionMapper;
import com.capgemini.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMessagePublisher messagePublisher;
    private final SessionMapper sessionMapper;

    @Override
    public SessionDto requestSession(SessionRequest request) {
        MentorshipSession session = MentorshipSession.builder()
                .mentorId(request.getMentorId())
                .learnerId(request.getLearnerId())
                .sessionDate(request.getSessionDate())
                .status("REQUESTED")
                .build();
                
        MentorshipSession savedSession = sessionRepository.save(session);
        publishEvent(savedSession);
        return sessionMapper.toDto(savedSession);
    }

    @Override
    public SessionDto updateSessionStatus(Long id, String status) {
        MentorshipSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
        session.setStatus(status.toUpperCase());
        MentorshipSession updatedSession = sessionRepository.save(session);
        
        publishEvent(updatedSession);
        return sessionMapper.toDto(updatedSession);
    }

    @Override
    public List<SessionDto> getSessionsByLearner(Long learnerId) {
        return sessionRepository.findByLearnerId(learnerId).stream()
                .map(sessionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionDto> getSessionsByMentor(Long mentorId) {
        return sessionRepository.findByMentorId(mentorId).stream()
                .map(sessionMapper::toDto)
                .collect(Collectors.toList());
    }

    private void publishEvent(MentorshipSession session) {
        SessionEvent event = SessionEvent.builder()
                .sessionId(session.getId())
                .mentorId(session.getMentorId())
                .learnerId(session.getLearnerId())
                .sessionTime(session.getSessionDate())
                .status(session.getStatus())
                .build();
        messagePublisher.publishSessionEvent(event);
    }

    @Override
    public void deleteSession(Long id) {
        MentorshipSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
        sessionRepository.delete(session);
    }
}
