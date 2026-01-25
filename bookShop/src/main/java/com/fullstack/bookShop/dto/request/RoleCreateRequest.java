package com.fullstack.bookShop.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleCreateRequest {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private Instant createdAt;
    private String createdBy;
}
