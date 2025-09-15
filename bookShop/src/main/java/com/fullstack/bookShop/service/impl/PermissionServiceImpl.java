package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.domain.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.domain.dto.permission.RestPermissionCreateDTO;
import com.fullstack.bookShop.domain.dto.permission.RestPermissionUpdateDTO;
import com.fullstack.bookShop.domain.dto.permission.RestPermissionViewDTO;
import com.fullstack.bookShop.domain.entity.Permission;
import com.fullstack.bookShop.repository.PermissionRepository;
import com.fullstack.bookShop.service.PermissionService;
import com.fullstack.bookShop.util.pagination.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Transactional
    @Override
    public Permission createPermission(Permission permission) {
        if (!this.checkNameExists(permission.getName())
                || this.checkApiPathAndMethodAndModuleExists(permission.getApiPath(), permission.getMethod(), permission.getModule())
        ) {
            throw new IllegalArgumentException(String.format("Permission with name '%s' already exists", permission.getName()));
        }
        return this.permissionRepository.save(permission);
    }

    @Transactional
    @Override
    public Permission updatePermission(Long id, Permission permissionUpdated) {
        Permission permissionGetById = this.getPermissionById(id);

        if (this.checkNameExists(permissionUpdated.getName())
                && !permissionGetById.getName().equals(permissionUpdated.getName())
        ) {
            throw new IllegalArgumentException(String.format("Permission with name '%s' already exists", permissionUpdated.getName()));
        }

        if (this.checkApiPathAndMethodAndModuleExists(permissionUpdated.getApiPath(), permissionUpdated.getMethod(), permissionUpdated.getModule())
                && !(permissionGetById.getApiPath().equals(permissionUpdated.getApiPath())
                    && permissionGetById.getModule().equals(permissionUpdated.getModule())
                    && permissionGetById.getMethod().equals(permissionUpdated.getMethod()))
        ) {
            throw new IllegalArgumentException(String.format("Permission with apiPath: '%s', method: '%s', module: '%s' already exists",
                    permissionUpdated.getApiPath(), permissionUpdated.getMethod(), permissionUpdated.getModule())
            );
        }

        permissionGetById.setName(permissionUpdated.getName());
        permissionGetById.setApiPath(permissionUpdated.getApiPath());
        permissionGetById.setMethod(permissionUpdated.getMethod());
        permissionGetById.setModule(permissionUpdated.getModule());

        return this.permissionRepository.save(permissionGetById);
    }

    @Override
    public Permission getPermissionById(Long id) {
        return this.permissionRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Permission with id " + id + " not found"));
    }

    @Override
    public ResultPaginationDTO fetchAllPermissions(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> pageHavPermissions = this.permissionRepository.findAll(spec, pageable);

        List<RestPermissionViewDTO> permissionViewDTOList = pageHavPermissions.getContent()
                .stream()
                .map(this::convertPermissionToRestPermissionViewDTO)
                .toList();

        ResultPaginationDTO resultPaginationDTO = PageUtil.handleFetchAllDataWithPagination(pageHavPermissions, pageable);
        resultPaginationDTO.setResult(permissionViewDTOList);

        return resultPaginationDTO;
    }

    @Transactional
    @Override
    public void deletePermissionById(Long id) {
        if (!this.checkIdExists(id)) {
            throw new NoSuchElementException("Permission with id " + id + " not found");
        }

        Permission permissionGetById = this.getPermissionById(id);
        permissionGetById.getRoles().forEach(role -> role
                .getPermissions().remove(permissionGetById)
        );

        this.permissionRepository.delete(permissionGetById);
    }

    @Override
    public boolean checkNameExists(String name) {
        return this.permissionRepository.existsByName(name);
    }

    @Override
    public boolean checkIdExists(Long id) {
        return this.permissionRepository.existsById(id);
    }

    @Override
    public boolean checkApiPathAndMethodAndModuleExists(String apiPath, String method, String module) {
        return this.permissionRepository.existsByApiPathAndMethodAndModule(apiPath, method, module);
    }

    @Override
    public RestPermissionCreateDTO convertPermissionToRestPermissionCreateDTO(Permission permission) {
        return new RestPermissionCreateDTO(
                permission.getId(),
                permission.getName(),
                permission.getApiPath(),
                permission.getMethod(),
                permission.getModule(),
                permission.getCreatedAt(),
                permission.getCreatedBy()
        );
    }

    @Override
    public RestPermissionUpdateDTO convertPermissionToRestPermissionUpdateDTO(Permission permission) {
        return new RestPermissionUpdateDTO(
                permission.getId(),
                permission.getName(),
                permission.getApiPath(),
                permission.getMethod(),
                permission.getModule(),
                permission.getUpdatedAt(),
                permission.getUpdatedBy()
        );
    }

    @Override
    public RestPermissionViewDTO convertPermissionToRestPermissionViewDTO(Permission permission) {
        return new RestPermissionViewDTO(
                permission.getId(),
                permission.getName(),
                permission.getApiPath(),
                permission.getMethod(),
                permission.getModule(),
                permission.getCreatedAt(),
                permission.getUpdatedAt(),
                permission.getCreatedBy(),
                permission.getUpdatedBy()
        );
    }
}
