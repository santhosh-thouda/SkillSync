package com.capgemini.mentor.service;

import com.capgemini.mentor.dto.AvailabilityUpdateRequest;
import com.capgemini.mentor.dto.MentorApplyRequest;
import com.capgemini.mentor.dto.MentorDto;

import java.util.List;

public interface MentorService {
    MentorDto applyForMentor(MentorApplyRequest request);
    List<MentorDto> getAllMentors();
    MentorDto getMentorById(Long id);
    MentorDto updateAvailability(Long id, AvailabilityUpdateRequest request);
    MentorDto approveMentor(Long id);
    void deleteMentor(Long id);
}
