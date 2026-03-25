package com.capgemini.user.mapper;

import com.capgemini.user.dto.UserDto;
import com.capgemini.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getProfileImage(),
                user.getCreatedAt()
        );
    }
}
