package com.fullstack.bookShop.dto.request.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    private Long id;
    private String fullName;
    private String email;
    private String avatar;
    private String phone;
    private String role;
    private Instant createdAt;
    private String createdBy;
}
