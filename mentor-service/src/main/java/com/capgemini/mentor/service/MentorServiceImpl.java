package com.capgemini.mentor.service;

import com.capgemini.mentor.dto.AvailabilityUpdateRequest;
import com.capgemini.mentor.dto.MentorApplyRequest;
import com.capgemini.mentor.dto.MentorDto;
import com.capgemini.mentor.entity.Mentor;
import com.capgemini.mentor.exception.ResourceNotFoundException;
import com.capgemini.mentor.mapper.MentorMapper;
import com.capgemini.mentor.repository.MentorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {

    private final MentorRepository mentorRepository;
    private final MentorMapper mentorMapper;

    @Override
    public MentorDto applyForMentor(MentorApplyRequest request) {
        // Here we could add logic to check if user exists (e.g., via FeignClient)
        // or check if a mentor profile already exists for the user.
        
        Mentor mentor = Mentor.builder()
                .userId(request.getUserId())
                .bio(request.getBio())
                .experience(request.getExperience())
                .hourlyRate(request.getHourlyRate())
                .skills(request.getSkills())
                .available(true)
                .build();
                
        Mentor savedMentor = mentorRepository.save(mentor);
        return mentorMapper.toDto(savedMentor);
    }

    @Override
    public List<MentorDto> getAllMentors() {
        return mentorRepository.findAll().stream()
                .map(mentorMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MentorDto getMentorById(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));
        return mentorMapper.toDto(mentor);
    }

    @Override
    public MentorDto updateAvailability(Long id, AvailabilityUpdateRequest request) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));
        
        mentor.setAvailable(request.isAvailable());
        Mentor updatedMentor = mentorRepository.save(mentor);
        return mentorMapper.toDto(updatedMentor);
    }

    @Override
    public MentorDto approveMentor(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));
        mentor.setApproved(true);
        Mentor updatedMentor = mentorRepository.save(mentor);
        return mentorMapper.toDto(updatedMentor);
    }

    @Override
    public void deleteMentor(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));
        mentorRepository.delete(mentor);
    }
}
