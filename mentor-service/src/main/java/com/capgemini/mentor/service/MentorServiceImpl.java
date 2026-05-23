package com.capgemini.mentor.service;

import com.capgemini.mentor.dto.AvailabilityUpdateRequest;
import com.capgemini.mentor.dto.MentorApplyRequest;
import com.capgemini.mentor.dto.MentorDto;
import com.capgemini.mentor.dto.MentorUpdateRequest;
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
        Mentor mentor = mentorRepository.findByUserId(request.getUserId())
                .orElseGet(() -> Mentor.builder() // User user = new User(); Mentor mentor = Mentor.builder().id().firstName().lastname()
                        .userId(request.getUserId())
                        .available(true)
                        .approved(false)
                        .build());

        if (request.getName() != null) {
            mentor.setName(request.getName());
        }
        if (request.getBio() != null) {
            mentor.setBio(request.getBio());
        }
        if (request.getExperience() != null) {
            mentor.setExperience(request.getExperience());
        }
        if (request.getHourlyRate() != null) {
            mentor.setHourlyRate(request.getHourlyRate());
        }
        if (request.getSkills() != null) {
            mentor.setSkills(request.getSkills());
        }
                
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
    public MentorDto getMentorByUserId(Long userId) {
        Mentor mentor = mentorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with user id: " + userId));
        return mentorMapper.toDto(mentor);
    }

    @Override
    public MentorDto updateMentor(Long id, MentorUpdateRequest request) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));

        mentor.setBio(request.getBio());
        mentor.setExperience(request.getExperience());
        mentor.setHourlyRate(request.getHourlyRate());
        mentor.setSkills(request.getSkills());
        if (request.getAvailable() != null) {
            mentor.setAvailable(request.getAvailable());
        }

        Mentor updatedMentor = mentorRepository.save(mentor);
        return mentorMapper.toDto(updatedMentor);
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
    public MentorDto addEarnings(Long id, Double amount) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));
        mentor.setEarnings((mentor.getEarnings() != null ? mentor.getEarnings() : 0.0) + amount);
        return mentorMapper.toDto(mentorRepository.save(mentor));
    }

    @Override
    public void deleteMentor(Long id) {
        Mentor mentor = mentorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found with id: " + id));
        mentorRepository.delete(mentor);
    }
}
