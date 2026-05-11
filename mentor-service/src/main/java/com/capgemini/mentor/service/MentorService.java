package com.capgemini.mentor.service;

import com.capgemini.mentor.dto.AvailabilityUpdateRequest;
import com.capgemini.mentor.dto.MentorApplyRequest;
import com.capgemini.mentor.dto.MentorDto;
import com.capgemini.mentor.dto.MentorUpdateRequest;

import java.util.List;

public interface MentorService {
    MentorDto applyForMentor(MentorApplyRequest request);
    List<MentorDto> getAllMentors();
    MentorDto getMentorById(Long id);
    MentorDto getMentorByUserId(Long userId);
    MentorDto updateMentor(Long id, MentorUpdateRequest request);
    MentorDto updateAvailability(Long id, AvailabilityUpdateRequest request);
    MentorDto approveMentor(Long id);
    MentorDto addEarnings(Long id, Double amount);
    void deleteMentor(Long id);
}
