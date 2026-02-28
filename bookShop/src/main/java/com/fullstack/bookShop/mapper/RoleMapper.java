package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.RoleResponse;
import com.fullstack.bookShop.model.Permission;
import com.fullstack.bookShop.model.Role;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RoleMapper {

    public RoleResponse toRoleResponse(Role role, String methodName) {
        RoleResponse.RoleResponseBuilder builder = RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .active(role.getActive())
                .permissions(convertListPermissions(role));

        if (ActionConstant.CREATE.equals(methodName)) {
            builder.createdAt(role.getCreatedAt())
                    .createdBy(role.getCreatedBy());
        } else if (ActionConstant.UPDATE.equals(methodName)) {
            builder.updatedAt(role.getUpdatedAt())
                    .updatedBy(role.getUpdatedBy());
        } else if (ActionConstant.FETCH.equals(methodName)) {
            builder.createdAt(role.getCreatedAt())
                    .updatedAt(role.getUpdatedAt())
                    .createdBy(role.getCreatedBy())
                    .updatedBy(role.getUpdatedBy());
        }

        return builder.build();
    }

    private List<String> convertListPermissions(Role role) {
        return Optional.ofNullable(role.getPermissions())
                .orElse(List.of())
                .stream()
                .map(Permission::getName)
                .toList();
    }
}
