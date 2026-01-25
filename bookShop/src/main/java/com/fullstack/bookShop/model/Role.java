package com.fullstack.bookShop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fullstack.bookShop.util.constant.auth.AuthorityConstant;
import com.fullstack.bookShop.util.constant.entity.RoleConstant;
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
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = RoleConstant.NAME_REQUIRED)
    @Size(max = 100, message = RoleConstant.NAME_MAX_LENGTH)
    private String name;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = RoleConstant.DESCRIPTION_REQUIRED)
    @Size(max = 5000, message = RoleConstant.DESCRIPTION_MAX_LENGTH)
    private String description;

    private Boolean active;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @JsonIgnoreProperties(value = {"roles"})
    private List<Permission> permissions;

    private Instant createdAt;

    private Instant updatedAt;

    private String createdBy;

    private String updatedBy;

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
