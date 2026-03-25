package com.capgemini.user.service;

import com.capgemini.user.dto.UserDto;
import com.capgemini.user.dto.UserUpdateRequest;
import java.util.List;

public interface UserService {
    UserDto getUserById(Long id);
    UserDto getUserByEmail(String email);
    List<UserDto> getAllUsers();
    UserDto updateUser(Long id, UserUpdateRequest updateRequest);
    UserDto createUser(UserDto request);
    void deleteUser(Long id);
}
