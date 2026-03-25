package com.capgemini.mentor.service;

import com.capgemini.mentor.dto.AvailabilityUpdateRequest;
import com.capgemini.mentor.dto.MentorApplyRequest;
import com.capgemini.mentor.dto.MentorDto;
import com.capgemini.mentor.entity.Mentor;
import com.capgemini.mentor.exception.ResourceNotFoundException;
import com.capgemini.mentor.mapper.MentorMapper;
import com.capgemini.mentor.repository.MentorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MentorServiceImplTest {

    @Mock
    private MentorRepository mentorRepository;

    @Spy
    private MentorMapper mentorMapper;

    @InjectMocks
    private MentorServiceImpl mentorService;

    @Test
    void applyForMentorShouldCreateAvailablePendingMentor() {
        MentorApplyRequest request = new MentorApplyRequest();
        request.setUserId(10L);
        request.setBio("Java mentor");
        request.setExperience(5);
        request.setHourlyRate(25.0);
        request.setSkills(List.of(1L, 2L));

        Mentor savedMentor = Mentor.builder()
                .id(1L)
                .userId(10L)
                .bio("Java mentor")
                .experience(5)
                .hourlyRate(25.0)
                .skills(List.of(1L, 2L))
                .available(true)
                .approved(false)
                .rating(0.0)
                .build();

        when(mentorRepository.save(any(Mentor.class))).thenReturn(savedMentor);

        MentorDto response = mentorService.applyForMentor(request);

        assertEquals(10L, response.getUserId());
        assertEquals(true, response.isAvailable());
        assertEquals(false, response.getApproved());
        assertEquals(List.of(1L, 2L), response.getSkills());
    }

    @Test
    void getMentorByIdShouldThrowWhenMissing() {
        when(mentorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> mentorService.getMentorById(99L));
    }

    @Test
    void updateAvailabilityShouldPersistNewFlag() {
        Mentor mentor = Mentor.builder()
                .id(1L)
                .userId(10L)
                .available(true)
                .approved(false)
                .build();
        AvailabilityUpdateRequest request = new AvailabilityUpdateRequest();
        request.setAvailable(false);

        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(mentorRepository.save(mentor)).thenReturn(mentor);

        MentorDto response = mentorService.updateAvailability(1L, request);

        assertEquals(false, response.isAvailable());
    }

    @Test
    void approveMentorShouldMarkMentorApproved() {
        Mentor mentor = Mentor.builder()
                .id(1L)
                .userId(10L)
                .approved(false)
                .available(true)
                .build();

        when(mentorRepository.findById(1L)).thenReturn(Optional.of(mentor));
        when(mentorRepository.save(mentor)).thenReturn(mentor);

        MentorDto response = mentorService.approveMentor(1L);

        assertEquals(true, response.getApproved());
    }

    @Test
    void getAllMentorsShouldMapEntities() {
        when(mentorRepository.findAll()).thenReturn(List.of(
                Mentor.builder().id(1L).userId(10L).bio("Java").available(true).approved(false).build(),
                Mentor.builder().id(2L).userId(20L).bio("Spring").available(false).approved(true).build()
        ));

        List<MentorDto> response = mentorService.getAllMentors();

        assertEquals(2, response.size());
        assertEquals(10L, response.get(0).getUserId());
        assertEquals(20L, response.get(1).getUserId());
    }
}
