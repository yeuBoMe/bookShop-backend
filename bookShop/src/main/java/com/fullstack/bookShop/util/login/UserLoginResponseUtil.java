package com.fullstack.bookShop.util.login;

import com.fullstack.bookShop.dto.response.auth.UserLoginResponse;
import com.fullstack.bookShop.model.Permission;
import com.fullstack.bookShop.model.User;

import java.util.List;
import java.util.Optional;

public final class UserLoginResponseUtil {

    private UserLoginResponseUtil() {}

    public static UserLoginResponse buildUserLogin(User user) {
        if (user == null) {
            return null;
        }

        if (user.getRole() != null) {
            List<String> permissions = Optional.ofNullable(user.getRole().getPermissions())
                    .orElse(List.of())
                    .stream()
                    .map(Permission::getName)
                    .toList();

            UserLoginResponse.RoleOfUserLoginResponse roleOfUserLoginResponse
                    = new UserLoginResponse.RoleOfUserLoginResponse(user.getRole().getName(), permissions);

            return new UserLoginResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getAvatar(),
                    user.getPhone(),
                    roleOfUserLoginResponse
            );
        }

        return null;
    }
}
