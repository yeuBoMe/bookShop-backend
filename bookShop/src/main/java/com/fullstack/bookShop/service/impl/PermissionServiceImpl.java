package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.response.PermissionResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.PermissionMapper;
import com.fullstack.bookShop.model.Permission;
import com.fullstack.bookShop.repository.PermissionRepository;
import com.fullstack.bookShop.service.interfaces.PermissionService;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.enums.PermissionEnum;
import com.fullstack.bookShop.util.pagination.PaginationUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final PermissionMapper permissionMapper;

    private final PermissionRepository permissionRepository;

    @Override
    public PaginationResponse fetchAllPermissions(Specification<Permission> spec, Pageable pageable) {
        Page<Permission> permissionPage = permissionRepository.findAll(spec, pageable);

        List<PermissionResponse> permissionResponseList = permissionPage.getContent()
                .stream()
                .map(permission -> permissionMapper.toResponse(permission, ActionConstant.FETCH))
                .toList();

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(permissionPage, pageable);
        paginationResponse.setResult(permissionResponseList);
        return paginationResponse;
    }

    @Transactional
    @Override
    public Permission createPermission(Permission permissionRequest) {
        LOGGER.info(
                "Creating permission name={}, apiPath={}, method={}",
                permissionRequest.getName(),
                permissionRequest.getApiPath(),
                permissionRequest.getMethod()
        );

        validatePermissionForCreate(permissionRequest);

        Permission permissionCreated = permissionRepository.save(permissionRequest);
        LOGGER.info("Create permission successfully with id: {}", permissionCreated.getId());

        return permissionCreated;
    }

    @Override
    public Permission getPermissionById(Long id) {
        LOGGER.debug("Fetching permission by id: {}", id);
        return permissionRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        String.format(PermissionEnum.ID_NOT_FOUND.getMessage(), id)
                ));
    }

    @Transactional
    @Override
    public Permission updatePermissionById(Long id, Permission permissionRequest) {
        LOGGER.info("Updating permission id: {}", id);
        Permission permissionGetById = getPermissionById(id);

        validatePermissionForUpdate(permissionGetById, permissionRequest);

        permissionGetById.setName(permissionRequest.getName());
        permissionGetById.setApiPath(permissionRequest.getApiPath());
        permissionGetById.setMethod(permissionRequest.getMethod());
        permissionGetById.setModule(permissionRequest.getModule());

        Permission permissionUpdated = permissionRepository.save(permissionGetById);
        LOGGER.info("Update permission successfully with id: {}", permissionUpdated.getId());

        return permissionUpdated;
    }

    @Transactional
    @Override
    public void deletePermissionById(Long id) {
        LOGGER.info("Deleting permission id: {}", id);
        Permission permissionGetById = getPermissionById(id);
        permissionGetById.getRoles().forEach(role -> role
                .getPermissions()
                .remove(permissionGetById)
        );
        permissionRepository.delete(permissionGetById);
        LOGGER.info("Delete permission successfully with id: {}", id);
    }

    @Override
    public boolean checkNameExistsIgnoreCase(String name) {
        return permissionRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean checkIdExists(Long id) {
        return permissionRepository.existsById(id);
    }

    @Override
    public boolean checkApiPathAndMethodAndModuleExists(String apiPath, String method, String module) {
        return permissionRepository.existsByApiPathAndMethodAndModule(apiPath, method, module);
    }

    private void validatePermissionForCreate(Permission permissionRequest) {
        if (checkNameExistsIgnoreCase(permissionRequest.getName())) {
            LOGGER.warn("Create failed: Permission name '{}' already exists", permissionRequest.getName());
            throw new IllegalArgumentException(
                    String.format(PermissionEnum.NAME_EXISTS.getMessage(), permissionRequest.getName())
            );
        }

        if (checkApiPathAndMethodAndModuleExists(
                permissionRequest.getApiPath(),
                permissionRequest.getMethod(),
                permissionRequest.getModule()
        )) {
            LOGGER.warn(
                    "Create failed: Permission API {}-{}-{} already exists",
                    permissionRequest.getApiPath(),
                    permissionRequest.getMethod(),
                    permissionRequest.getModule()
            );
            throw new IllegalArgumentException(
                    String.format(
                            PermissionEnum.API_EXISTS.getMessage(),
                            permissionRequest.getApiPath(),
                            permissionRequest.getMethod(),
                            permissionRequest.getModule()
                    )
            );
        }
    }

    private void validatePermissionForUpdate(Permission currentPermission, Permission permissionRequest) {
        boolean isNameExists = checkNameExistsIgnoreCase(permissionRequest.getName());

        if (isNameExists && !permissionRequest.getName().equals(currentPermission.getName())) {
            LOGGER.warn("Update failed: Permission name '{}' already exists", permissionRequest.getName());
            throw new IllegalArgumentException(
                    String.format(PermissionEnum.NAME_EXISTS.getMessage(), permissionRequest.getName())
            );
        }

        boolean isApiExists = checkApiPathAndMethodAndModuleExists(
                permissionRequest.getApiPath(),
                permissionRequest.getMethod(),
                permissionRequest.getModule()
        );

        if (isApiExists && !(
                permissionRequest.getApiPath().equals(currentPermission.getApiPath())
                        && permissionRequest.getMethod().equals(currentPermission.getMethod())
                        && permissionRequest.getModule().equals(currentPermission.getModule())
        )) {
            LOGGER.warn(
                    "Update failed: Permission API {}-{}-{} already exists",
                    permissionRequest.getApiPath(),
                    permissionRequest.getMethod(),
                    permissionRequest.getModule()
            );
            throw new IllegalArgumentException(
                    String.format(
                            PermissionEnum.API_EXISTS.getMessage(),
                            permissionRequest.getApiPath(),
                            permissionRequest.getMethod(),
                            permissionRequest.getModule()
                    )
            );
        }
    }
}
