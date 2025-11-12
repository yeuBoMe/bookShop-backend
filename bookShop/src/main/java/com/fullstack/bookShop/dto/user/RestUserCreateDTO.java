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
public class RestUserCreateDTO {
    private Long id;
    private String fullName;
    private String email;
    private String avatar;
    private String phone;
    private Instant createdAt;
    private String createdBy;
    private RoleUserCreate role;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RoleUserCreate {
        private Long id;
        private String name;
    }
}
