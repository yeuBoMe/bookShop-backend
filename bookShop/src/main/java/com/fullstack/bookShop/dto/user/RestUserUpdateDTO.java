package com.fullstack.bookShop.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestUserUpdateDTO {
    private Long id;
    private String fullName;
    private String email;
    private String avatar;
    private String phone;
    private Instant updatedAt;
    private String updatedBy;
    private RoleUserUpdate role;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleUserUpdate {
        private Long id;
        private String name;
    }
}
