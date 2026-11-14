package com.fullstack.bookShop.model;

import com.fullstack.bookShop.model.base.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fullstack.bookShop.util.constant.entity.RoleConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(
        name = "roles",
        indexes = @Index(name = "idx_roles_name", columnList = "name")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roles_id_seq")
    @SequenceGenerator(name = "roles_id_seq", sequenceName = "roles_id_seq", allocationSize = 50)
    private Long id;

    @NotBlank(message = RoleConstant.NAME_REQUIRED)
    @Size(max = 100, message = RoleConstant.NAME_MAX_LENGTH)
    private String name;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = RoleConstant.DESCRIPTION_REQUIRED)
    @Size(max = 5000, message = RoleConstant.DESCRIPTION_MAX_LENGTH)
    private String description;

    @NotNull(message = RoleConstant.ACTIVE_REQUIRED)
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
}
