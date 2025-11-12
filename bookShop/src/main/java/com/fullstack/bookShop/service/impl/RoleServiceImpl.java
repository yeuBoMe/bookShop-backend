package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.dto.role.RestRoleCreateDTO;
import com.fullstack.bookShop.dto.role.RestRoleUpdateDTO;
import com.fullstack.bookShop.dto.role.RestRoleViewDTO;
import com.fullstack.bookShop.model.Permission;
import com.fullstack.bookShop.model.Role;
import com.fullstack.bookShop.repository.PermissionRepository;
import com.fullstack.bookShop.repository.RoleRepository;
import com.fullstack.bookShop.repository.UserRepository;
import com.fullstack.bookShop.service.RoleService;
import com.fullstack.bookShop.util.pagination.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final PermissionRepository permissionRepository;

    private void checkNullAndSetListPermissions(Role roleRequest, Role currentRole) {
        if (!roleRequest.getPermissions().isEmpty()) {
            for (Permission permission : roleRequest.getPermissions()) {
                if (!permissionRepository.existsById(permission.getId())) {
                    throw new NoSuchElementException("Permission with id " + permission.getId() + " not found!");
                }
            }

            List<Long> permissionIds = roleRequest.getPermissions()
                    .stream()
                    .map(Permission::getId)
                    .toList();

            List<Permission> permissionList = permissionRepository.findALlByIdIn(permissionIds);
            currentRole.setPermissions(permissionList);
        }
    }

    @Transactional
    @Override
    public Role createRole(Role role) {
        if (checkNameExistsIgnoreCase(role.getName())) {
            throw new IllegalArgumentException(String.format("Role with name '%s' already exists!", role.getName()));
        }
        checkNullAndSetListPermissions(role, role);
        return roleRepository.save(role);
    }

    @Transactional
    @Override
    public Role updateRoleById(Long id, Role roleUpdated) {
        Role roleGetById = getRoleById(id);
        checkNullAndSetListPermissions(roleUpdated, roleGetById);

        if (checkNameExistsIgnoreCase(roleUpdated.getName())
            && !roleGetById.getName().equals(roleUpdated.getName())
        ) {
            throw new IllegalArgumentException(String.format("Role with name '%s' already exists!", roleUpdated.getName()));
        }

        roleGetById.setName(roleUpdated.getName());
        roleGetById.setActive(roleUpdated.isActive());
        roleGetById.setDescription(roleUpdated.getDescription());

        return roleRepository.save(roleGetById);
    }

    @Override
    public Role getRoleById(Long id) {
        return roleRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Role with id " + id + " not found!"));
    }

    @Transactional
    @Override
    public void deleteRoleById(Long id) {
        if (!checkIdExists(id)) {
            throw new NoSuchElementException("Role with id " + id + " not found!");
        }
        userRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
    }

    @Override
    public boolean checkIdExists(Long id) {
        return roleRepository.existsById(id);
    }

    @Override
    public boolean checkNameExistsIgnoreCase(String name) {
        return roleRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public RestRoleCreateDTO convertRoleToRestRoleCreateDTO(Role role) {
        List<String> permissionList = Optional.ofNullable(role.getPermissions())
                .map(permissions -> permissions.stream()
                        .map(Permission::getName)
                        .toList()
                )
                .orElse(new ArrayList<>());

        return new RestRoleCreateDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                role.getCreatedAt(),
                role.getCreatedBy(),
                permissionList
        );
    }

    @Override
    public RestRoleUpdateDTO convertRoleToRestRoleUpdateDTO(Role role) {
        List<String> permissionList = Optional.ofNullable(role.getPermissions())
                .map(permissions -> permissions.stream()
                        .map(Permission::getName)
                        .toList()
                )
                .orElse(new ArrayList<>());

        return new RestRoleUpdateDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                role.getUpdatedAt(),
                role.getUpdatedBy(),
                permissionList
        );
    }

    @Override
    public RestRoleViewDTO convertRoleToRestRoleViewDTO(Role role) {
        List<String> permissionList = Optional.ofNullable(role.getPermissions())
                .map(permissions -> permissions.stream()
                        .map(Permission::getName)
                        .toList()
                )
                .orElse(new ArrayList<>());

        return new RestRoleViewDTO(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.isActive(),
                role.getCreatedAt(),
                role.getUpdatedAt(),
                role.getCreatedBy(),
                role.getUpdatedBy(),
                permissionList
        );
    }

    @Override
    public ResultPaginationDTO fetchAllRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> pageHavRoles = roleRepository.findAll(spec, pageable);

        List<RestRoleViewDTO> restRoleViewDTOList = pageHavRoles.getContent()
                .stream()
                .map(this::convertRoleToRestRoleViewDTO)
                .toList();

        ResultPaginationDTO resultPaginationDTO = PageUtil.handleFetchAllDataWithPagination(pageHavRoles, pageable);
        resultPaginationDTO.setResult(restRoleViewDTOList);

        return resultPaginationDTO;
    }
}
