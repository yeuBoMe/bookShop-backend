package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.UserResponse;
import com.fullstack.bookShop.model.User;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserMapper {

    public UserResponse toResponse(User user, String methodName) {
        UserResponse.UserResponseBuilder builder = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .phone(user.getPhone())
                .role(convertRole(user));

        if (ActionConstant.CREATE.equals(methodName)) {
            builder.createdAt(user.getCreatedAt())
                    .createdBy(user.getCreatedBy());
        } else if (ActionConstant.UPDATE.equals(methodName)) {
            builder.updatedAt(user.getUpdatedAt())
                    .updatedBy(user.getUpdatedBy());
        } else if (ActionConstant.FETCH.equals(methodName)) {
            builder.createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .createdBy(user.getCreatedBy())
                    .updatedBy(user.getUpdatedBy());
        }

        return builder.build();
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
