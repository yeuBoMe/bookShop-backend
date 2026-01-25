package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.model.Permission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface PermissionService {

    PaginationResponse fetchAllPermissions(Specification<Permission> spec, Pageable pageable);

    Permission createPermission(Permission permissionRequest);

    Permission getPermissionById(Long id);

    Permission updatePermissionById(Long id, Permission permissionRequest);

    void deletePermissionById(Long id);

    boolean checkNameExistsIgnoreCase(String name);

    boolean checkIdExists(Long id);

    boolean checkApiPathAndMethodAndModuleExists(String apiPath, String method, String module);
}
