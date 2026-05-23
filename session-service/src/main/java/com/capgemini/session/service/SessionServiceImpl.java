package com.capgemini.session.service;

import com.capgemini.session.client.MentorServiceClient;
import com.capgemini.session.dto.SessionDto;
import com.capgemini.session.dto.SessionRequest;
import com.capgemini.session.entity.MentorshipSession;
import com.capgemini.session.event.SessionEvent;
import com.capgemini.session.exception.ResourceNotFoundException;
import com.capgemini.session.mapper.SessionMapper;
import com.capgemini.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link SessionService}.
 * Handles the business logic for creating and transitioning the state of mentorship sessions.
 * Interacts synchronously with MentorService via Feign to update earnings,
 * and asynchronously with NotificationService via RabbitMQ to dispatch emails.
 */
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMessagePublisher messagePublisher;
    private final SessionMapper sessionMapper;
    private final MentorServiceClient mentorServiceClient;
    private final HttpServletRequest request;

    /**
     * Initializes a new mentorship session in the PENDING state.
     * Publishes a SessionCreated event to RabbitMQ for notifications.
     *
     * @param request The session request details.
     * @return The saved SessionDto.
     */
    @Override
    public SessionDto requestSession(SessionRequest request) {
        MentorshipSession session = MentorshipSession.builder()
                .mentorId(request.getMentorId())
                .learnerId(request.getLearnerId())
                .mentorName(request.getMentorName())
                .learnerName(request.getLearnerName())
                .hourlyRate(request.getHourlyRate())
                .sessionDate(request.getSessionDate())
                .status("REQUESTED")
                .build();
                
        MentorshipSession savedSession = sessionRepository.save(session);
        publishEvent(savedSession);
        return sessionMapper.toDto(savedSession);
    }

    /**
     * Updates the status of an existing session (e.g., ACCEPTED, REJECTED, COMPLETED).
     * If the status is 'COMPLETED', it makes a synchronous Feign call to the mentor-service
     * to increment the mentor's total earnings.
     * Finally, publishes a status change event to RabbitMQ.
     *
     * @param id The session ID.
     * @param status The new status string.
     * @return The updated SessionDto.
     * @throws ResourceNotFoundException if the session does not exist.
     */
    @Override
    public SessionDto updateSessionStatus(Long id, String status) {
        MentorshipSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found with id: " + id));
        session.setStatus(status.toUpperCase());
        MentorshipSession updatedSession = sessionRepository.save(session);
        
        if ("COMPLETED".equalsIgnoreCase(status)) {
            try {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null) {
                    Double rate = updatedSession.getHourlyRate();
                    
                    // Fallback to mentor's current rate if session rate is missing
                    if (rate == null) {
                        try {
                            com.capgemini.session.dto.MentorDto mentor = mentorServiceClient.getMentorById(authHeader, updatedSession.getMentorId());
                            if (mentor != null) {
                                rate = mentor.getHourlyRate();
                            }
                        } catch (Exception e) {
                            System.err.println("Could not fetch mentor rate for earnings: " + e.getMessage());
                        }
                    }
                    
                    
                    if (rate != null) {
                        mentorServiceClient.addEarnings(authHeader, updatedSession.getMentorId(), rate);
                    }
                }
            } catch (Exception e) {
                // Log error but don't fail session update
                System.err.println("Failed to update mentor earnings: " + e.getMessage());
            }
        }

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

    /**
     * Helper method to map a MentorshipSession entity to a SessionEvent and
     * publish it to the RabbitMQ topic exchange.
     *
     * @param session The updated session entity.
     */
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
