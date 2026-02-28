package com.fullstack.bookShop.mapper;

import com.fullstack.bookShop.dto.response.PermissionResponse;
import com.fullstack.bookShop.model.Permission;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionResponse toResponse(Permission permission, String methodName) {
        PermissionResponse.PermissionResponseBuilder builder = PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .apiPath(permission.getApiPath())
                .method(permission.getMethod())
                .module(permission.getModule());

        if (ActionConstant.CREATE.equals(methodName)) {
            builder.createdAt(permission.getCreatedAt())
                    .createdBy(permission.getCreatedBy());
        } else if (ActionConstant.UPDATE.equals(methodName)) {
            builder.updatedAt(permission.getUpdatedAt())
                    .updatedBy(permission.getUpdatedBy());
        } else if (ActionConstant.FETCH.equals(methodName)) {
            builder.createdAt(permission.getCreatedAt())
                    .updatedAt(permission.getUpdatedAt())
                    .createdBy(permission.getCreatedBy())
                    .updatedBy(permission.getUpdatedBy());
        }

        return builder.build();
    }
}
