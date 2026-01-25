package com.fullstack.bookShop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.constant.entity.PermissionConstant;
import com.fullstack.bookShop.util.security.SecurityUtil;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = PermissionConstant.NAME_REQUIRED)
    @Size(max = 100, message = PermissionConstant.NAME_MAX_LENGTH)
    private String name;

    @NotBlank(message = PermissionConstant.API_PATH_REQUIRED)
    private String apiPath;

    @NotBlank(message = PermissionConstant.METHOD_REQUIRED)
    private String method;

    @NotBlank(message = PermissionConstant.MODULE_REQUIRED)
    private String module;

    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Role> roles;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

    public Permission(
            String name,
            String apiPath,
            String method,
            String module
    ) {
        this.name = name;
        this.apiPath = apiPath;
        this.method = method;
        this.module = module;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.createdBy = SecurityUtil
                .getCurrentUserLoginEmail()
                .orElse(AuthorityConstant.SYSTEM);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
        this.updatedBy = SecurityUtil
                .getCurrentUserLoginEmail()
                .orElse(AuthorityConstant.SYSTEM);
    }
}
