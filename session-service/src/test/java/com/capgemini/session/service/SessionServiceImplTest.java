package com.capgemini.session.service;

import com.capgemini.session.dto.SessionDto;
import com.capgemini.session.dto.SessionRequest;
import com.capgemini.session.entity.MentorshipSession;
import com.capgemini.session.event.SessionEvent;
import com.capgemini.session.exception.ResourceNotFoundException;
import com.capgemini.session.mapper.SessionMapper;
import com.capgemini.session.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceImplTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private SessionMessagePublisher messagePublisher;

    @Spy
    private SessionMapper sessionMapper;

    @InjectMocks
    private SessionServiceImpl sessionService;

    @Test
    void requestSessionShouldPersistAndPublishRequestedEvent() {
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 4, 1, 10, 0);
        SessionRequest request = new SessionRequest();
        request.setMentorId(5L);
        request.setLearnerId(9L);
        request.setSessionDate(scheduledAt);

        MentorshipSession savedSession = MentorshipSession.builder()
                .id(1L)
                .mentorId(5L)
                .learnerId(9L)
                .sessionDate(scheduledAt)
                .status("REQUESTED")
                .build();

        when(sessionRepository.save(any(MentorshipSession.class))).thenReturn(savedSession);

        SessionDto response = sessionService.requestSession(request);

        assertEquals("REQUESTED", response.getStatus());
        ArgumentCaptor<SessionEvent> eventCaptor = ArgumentCaptor.forClass(SessionEvent.class);
        verify(messagePublisher).publishSessionEvent(eventCaptor.capture());
        assertEquals(1L, eventCaptor.getValue().getSessionId());
        assertEquals("REQUESTED", eventCaptor.getValue().getStatus());
    }

    @Test
    void updateSessionStatusShouldUppercaseAndPublishEvent() {
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 4, 1, 10, 0);
        MentorshipSession session = MentorshipSession.builder()
                .id(1L)
                .mentorId(5L)
                .learnerId(9L)
                .sessionDate(scheduledAt)
                .status("REQUESTED")
                .build();

        when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));
        when(sessionRepository.save(session)).thenReturn(session);

        SessionDto response = sessionService.updateSessionStatus(1L, "accepted");

        assertEquals("ACCEPTED", response.getStatus());
        ArgumentCaptor<SessionEvent> eventCaptor = ArgumentCaptor.forClass(SessionEvent.class);
        verify(messagePublisher).publishSessionEvent(eventCaptor.capture());
        assertEquals("ACCEPTED", eventCaptor.getValue().getStatus());
    }

    @Test
    void updateSessionStatusShouldThrowWhenMissing() {
        when(sessionRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> sessionService.updateSessionStatus(404L, "accepted"));
    }

    @Test
    void getSessionsByLearnerShouldMapResults() {
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 4, 1, 10, 0);
        when(sessionRepository.findByLearnerId(9L)).thenReturn(List.of(
                MentorshipSession.builder().id(1L).mentorId(5L).learnerId(9L).sessionDate(scheduledAt).status("REQUESTED").build()
        ));

        List<SessionDto> response = sessionService.getSessionsByLearner(9L);

        assertEquals(1, response.size());
        assertEquals(9L, response.get(0).getLearnerId());
    }

    @Test
    void getSessionsByMentorShouldMapResults() {
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 4, 1, 10, 0);
        when(sessionRepository.findByMentorId(5L)).thenReturn(List.of(
                MentorshipSession.builder().id(1L).mentorId(5L).learnerId(9L).sessionDate(scheduledAt).status("REQUESTED").build()
        ));

        List<SessionDto> response = sessionService.getSessionsByMentor(5L);

        assertEquals(1, response.size());
        assertEquals(5L, response.get(0).getMentorId());
    }
}
