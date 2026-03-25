package com.capgemini.session.service;

import com.capgemini.session.dto.SessionDto;
import com.capgemini.session.dto.SessionRequest;

import java.util.List;

public interface SessionService {
    SessionDto requestSession(SessionRequest request);
    SessionDto updateSessionStatus(Long id, String status);
    List<SessionDto> getSessionsByLearner(Long learnerId);
    List<SessionDto> getSessionsByMentor(Long mentorId);
    void deleteSession(Long id);
}
