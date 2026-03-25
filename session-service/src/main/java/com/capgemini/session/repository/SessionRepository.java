package com.capgemini.session.repository;

import com.capgemini.session.entity.MentorshipSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<MentorshipSession, Long> {
    List<MentorshipSession> findByLearnerId(Long learnerId);
    List<MentorshipSession> findByMentorId(Long mentorId);
}
