package com.fullstack.bookShop.model;

import com.fullstack.bookShop.model.base.AuditableEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fullstack.bookShop.util.constant.entity.PermissionConstant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(
        name = "permissions",
        indexes = {
                @Index(name = "idx_permissions_name", columnList = "name"),
                @Index(name = "idx_permissions_api_path_method", columnList = "api_path, method, module")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permissions_id_seq")
    @SequenceGenerator(name = "permissions_id_seq", sequenceName = "permissions_id_seq", allocationSize = 50)
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
}
