package com.capgemini.user.service;

import com.capgemini.user.dto.UserDto;
import com.capgemini.user.dto.UserUpdateRequest;
import com.capgemini.user.entity.User;
import com.capgemini.user.exception.ResourceNotFoundException;
import com.capgemini.user.mapper.UserMapper;
import com.capgemini.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserShouldPersistDefaultPasswordAndReturnDto() {
        UserDto request = new UserDto(null, "Alex", "alex@example.com", "LEARNER", null, null);

        User savedUser = User.builder()
                .id(1L)
                .name("Alex")
                .email("alex@example.com")
                .password("N/A")
                .role("LEARNER")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto response = userService.createUser(request);

        assertEquals(1L, response.getId());
        assertEquals("Alex", response.getName());
        assertEquals("alex@example.com", response.getEmail());
        assertEquals("LEARNER", response.getRole());
    }

    @Test
    void getUserByEmailShouldMapEntity() {
        User user = User.builder()
                .id(2L)
                .name("Taylor")
                .email("taylor@example.com")
                .password("N/A")
                .role("MENTOR")
                .profileImage("profile.png")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("taylor@example.com")).thenReturn(Optional.of(user));

        UserDto response = userService.getUserByEmail("taylor@example.com");

        assertEquals("Taylor", response.getName());
        assertEquals("MENTOR", response.getRole());
    }

    @Test
    void updateUserShouldModifyNameAndProfileImage() {
        User existing = User.builder()
                .id(3L)
                .name("Old Name")
                .email("old@example.com")
                .password("N/A")
                .role("LEARNER")
                .build();
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("New Name");
        request.setProfileImage("avatar.png");

        when(userRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        UserDto response = userService.updateUser(3L, request);

        assertEquals("New Name", response.getName());
        assertEquals("avatar.png", response.getProfileImage());
    }

    @Test
    void getUserByIdShouldThrowWhenMissing() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(404L));
    }

    @Test
    void getAllUsersShouldMapAllEntities() {
        when(userRepository.findAll()).thenReturn(List.of(
                User.builder().id(1L).name("Alex").email("alex@example.com").password("N/A").role("LEARNER").build(),
                User.builder().id(2L).name("Taylor").email("taylor@example.com").password("N/A").role("MENTOR").build()
        ));

        List<UserDto> response = userService.getAllUsers();

        assertEquals(2, response.size());
        assertEquals("Alex", response.get(0).getName());
        assertEquals("Taylor", response.get(1).getName());
    }
}
