package com.fullstack.bookShop.service;

import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.dto.permission.RestPermissionCreateDTO;
import com.fullstack.bookShop.dto.permission.RestPermissionUpdateDTO;
import com.fullstack.bookShop.dto.permission.RestPermissionViewDTO;
import com.fullstack.bookShop.model.Permission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface PermissionService {
    Permission createPermission(Permission permission);
    Permission updatePermission(Long id, Permission permission);
    Permission getPermissionById(Long id);

    ResultPaginationDTO fetchAllPermissions(Specification<Permission> spec, Pageable pageable);

    void deletePermissionById(Long id);

    boolean checkNameExistsIgnoreCase(String name);
    boolean checkIdExists(Long id);
    boolean checkApiPathAndMethodAndModuleExists(String apiPath, String method, String module);

    RestPermissionCreateDTO convertPermissionToRestPermissionCreateDTO(Permission permission);
    RestPermissionUpdateDTO convertPermissionToRestPermissionUpdateDTO(Permission permission);
    RestPermissionViewDTO convertPermissionToRestPermissionViewDTO(Permission permission);
}
