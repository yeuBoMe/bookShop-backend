package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.UserResponse;
import com.fullstack.bookShop.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMapper {

    public UserResponse convertUserToCreateResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .role(convertRole(user))
                .createdAt(user.getCreatedAt())
                .createdBy(user.getCreatedBy())
                .build();
    }

    public UserResponse convertUserToUpdateResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .role(convertRole(user))
                .updatedAt(user.getUpdatedAt())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    public UserResponse convertUserToViewResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .role(convertRole(user))
                .createdAt(user.getCreatedAt())
                .createdBy(user.getCreatedBy())
                .updatedAt(user.getUpdatedAt())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    private UserResponse.RoleOfUserResponse convertRole(User user) {
        return Optional.ofNullable(user.getRole())
                .map(role -> new UserResponse.RoleOfUserResponse(
                        role.getId(),
                        role.getName()
                ))
                .orElse(null);
    }
}
