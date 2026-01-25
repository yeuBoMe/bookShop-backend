package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.RoleResponse;
import com.fullstack.bookShop.model.Permission;
import com.fullstack.bookShop.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RoleMapper {

    public RoleResponse convertRoleToCreateResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .permissions(convertListPermissions(role))
                .createdAt(role.getCreatedAt())
                .createdBy(role.getCreatedBy())
                .build();
    }

    public RoleResponse convertRoleToUpdateResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .permissions(convertListPermissions(role))
                .updatedAt(role.getUpdatedAt())
                .updatedBy(role.getUpdatedBy())
                .build();
    }

    public RoleResponse convertRoleToViewResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .permissions(convertListPermissions(role))
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .createdBy(role.getCreatedBy())
                .updatedBy(role.getUpdatedBy())
                .build();
    }

    private List<String> convertListPermissions(Role role) {
        return Optional.ofNullable(role.getPermissions())
                .orElse(List.of())
                .stream()
                .map(Permission::getName)
                .toList();
    }
}
