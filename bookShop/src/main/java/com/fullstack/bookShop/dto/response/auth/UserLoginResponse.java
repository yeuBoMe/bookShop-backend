package com.fullstack.bookShop.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
    private Long id;
    private String name;
    private String email;
    private String avatar;
    private String phone;
    private RoleOfUserLoginResponse role;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleOfUserLoginResponse {
        private String name;
        private List<String> permissions;
    }
}
