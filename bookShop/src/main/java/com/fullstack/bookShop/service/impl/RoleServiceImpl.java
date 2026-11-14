package com.fullstack.bookShop.service.impl;

import com.fullstack.bookShop.dto.request.RoleCreateRequest;
import com.fullstack.bookShop.dto.response.RoleResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.mapper.RoleMapper;
import com.fullstack.bookShop.model.Permission;
import com.fullstack.bookShop.model.Role;
import com.fullstack.bookShop.repository.PermissionRepository;
import com.fullstack.bookShop.repository.RoleRepository;
import com.fullstack.bookShop.repository.UserRepository;
import com.fullstack.bookShop.service.interfaces.RoleService;
import com.fullstack.bookShop.util.constant.CommonMessage;
import com.fullstack.bookShop.util.constant.action.ActionConstant;
import com.fullstack.bookShop.util.enums.PermissionEnum;
import com.fullstack.bookShop.util.enums.RoleEnum;
import com.fullstack.bookShop.util.pagination.PaginationUtil;
import com.fullstack.bookShop.util.message.MessageTranslator;
import com.fullstack.bookShop.util.string.StringUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final MessageTranslator messageTranslator;

    private final RoleMapper roleMapper;

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final PermissionRepository permissionRepository;

    /*
     * Doc trong transaction de Session con song suot qua trinh map sang
     * RoleResponse (RoleMapper phai duyet role.permissions).
     *
     * Khong dung fetch join o day: fetch join mot collection kem Pageable se
     * khien Hibernate phan trang trong bo nho (canh bao HHH000104). Nho
     * hibernate.default_batch_fetch_size da bat trong application.properties,
     * permissions cua ca trang duoc nap gop chi bang them 1 cau query.
     */
    @Transactional(readOnly = true)
    @Override
    public PaginationResponse fetchAllRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> rolePage = roleRepository.findAll(spec, pageable);

        List<RoleResponse> roleResponseList = rolePage.getContent()
                .stream()
                .map(role -> roleMapper.toRoleResponse(role, ActionConstant.FETCH))
                .toList();

        PaginationResponse paginationResponse = PaginationUtil.fetchAllDataWithPagination(rolePage, pageable);
        paginationResponse.setResult(roleResponseList);
        return paginationResponse;
    }

    @Transactional
    @Override
    public Role createRole(Role roleRequest) {
        LOGGER.info(
                "Creating role name={}, description={}, active={}, permissions={}",
                roleRequest.getName(),
                roleRequest.getDescription(),
                roleRequest.getActive(),
                roleRequest.getPermissions()
        );

        validateRoleForCreate(roleRequest);
        validateAndSetPermissionsForRole(roleRequest, roleRequest);

        Role roleCreated = roleRepository.save(roleRequest);
        LOGGER.info("Create role successfully with id: {}", roleCreated.getId());

        return roleCreated;
    }

    @Override
    public Role getRoleById(Long id) {
        LOGGER.debug("Fetching role by id: {}", id);
        return roleRepository
                .findByIdFetchPermissions(id)
                .orElseThrow(() -> new NoSuchElementException(
                        messageTranslator.toLocale(RoleEnum.ID_NOT_FOUND.getMessageKey(), id)
                ));
    }

    @Override
    public Role getRoleByName(String name) {
        LOGGER.debug("Fetching role by name: {}", name);
        return roleRepository
                .findByName(name)
                .orElseThrow(() -> new NoSuchElementException(
                        messageTranslator.toLocale(RoleEnum.NAME_NOT_FOUND.getMessageKey(), name)
                ));
    }

    @Transactional
    @Override
    public Role updateRoleById(Long id, Role roleRequest) {
        LOGGER.info("Updating role id: {}", id);
        Role roleGetById = getRoleById(id);

        validateRoleForUpdate(roleGetById, roleRequest);
        validateAndSetPermissionsForRole(roleGetById, roleRequest);

        roleGetById.setName(roleRequest.getName());
        roleGetById.setDescription(roleRequest.getDescription());
        roleGetById.setActive(roleRequest.getActive());

        Role roleUpdated = roleRepository.save(roleGetById);
        LOGGER.info("Update role successfully with id: {}", roleUpdated.getId());

        return roleUpdated;
    }

    @Transactional
    @Override
    public void deleteRoleById(Long id) {
        LOGGER.info("Deleting role id: {}", id);
        if (!checkIdExists(id)) {
            throw new NoSuchElementException(
                    messageTranslator.toLocale(RoleEnum.ID_NOT_FOUND.getMessageKey(), id)
            );
        }
        userRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
        LOGGER.info("Delete role successfully with id: {}", id);
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
    public Set<String> findExistingNamesIgnoreCase(Set<String> names) {
        return roleRepository.findExistingNamesIgnoreCase(names);
    }

    @Transactional
    @Override
    public ImportFileResponse<RoleResponse> importRolesFromFile(List<RoleCreateRequest> roleCreateRequestList) {

        ImportFileResponse<RoleResponse> importFileResponse = new ImportFileResponse<>();
        List<RoleResponse> successRoles = new ArrayList<>();
        List<ImportFileResponse.DetailFaultInfo<RoleCreateRequest>> detailFaultInfoList = new ArrayList<>();
        List<Role> listRolesToSave = new ArrayList<>();

        int countSuccess = 0;
        int countError = 0;

        Set<String> existingNamesIgnoreCase = getExistingNamesIgnoreCaseFromRoleCreateRequestList(roleCreateRequestList);

        for (int i = 0; i < roleCreateRequestList.size(); i++) {
            RoleCreateRequest roleCreateRequest = roleCreateRequestList.get(i);
            int index = i + 1;

            try {
                validateRoleForImport(existingNamesIgnoreCase, roleCreateRequest);

                // Build Role from RoleCreateRequest
                Role roleBuiltFromRoleCreateRequest = buildRoleFromRoleCreateRequest(roleCreateRequest);
                listRolesToSave.add(roleBuiltFromRoleCreateRequest);

                RoleResponse roleResponse = roleMapper.toRoleResponse(roleBuiltFromRoleCreateRequest, ActionConstant.CREATE);
                successRoles.add(roleResponse);
                countSuccess++;

                LOGGER.info("Import role successfully with index {}: {}", index, roleCreateRequest.getName());

            } catch (ConstraintViolationException ex) {
                List<String> errorMessage = ex.getConstraintViolations()
                        .stream()
                        .map(ConstraintViolation::getMessage)
                        .toList();

                detailFaultInfoList.add(new ImportFileResponse.DetailFaultInfo<>(
                        index,
                        errorMessage,
                        roleCreateRequest
                ));

                countError++;
                LOGGER.warn("Import failed at row {} due to validation: {}", index, errorMessage);

            } catch (Exception ex) {
                detailFaultInfoList.add(new ImportFileResponse.DetailFaultInfo<>(
                        index,
                        ex.getMessage(),
                        roleCreateRequest
                ));

                countError++;
                LOGGER.error("Unexpected error importing role at row {}: {}", index, ex.getMessage(), ex);
            }
        }

        if (!listRolesToSave.isEmpty()) {
            roleRepository.saveAll(listRolesToSave);
            LOGGER.info("List roles saved {} roles successfully", listRolesToSave.size());
        }

        importFileResponse.setCountSuccess(countSuccess);
        importFileResponse.setCountError(countError);
        importFileResponse.setDetail(countError > 0 ? detailFaultInfoList : messageTranslator.toLocale(CommonMessage.IMPORT_SUCCESS));
        importFileResponse.setItems(successRoles);

        LOGGER.info("Import roles completed: {} success, {} failed", countSuccess, countError);
        return importFileResponse;
    }

    private void validateRoleForCreate(Role roleRequest) {
        if (checkNameExistsIgnoreCase(roleRequest.getName())) {
            LOGGER.warn("Create failed: Role name '{}' already exists", roleRequest.getName());
            throw new IllegalArgumentException(
                    messageTranslator.toLocale(RoleEnum.NAME_EXISTS.getMessageKey(), roleRequest.getName())
            );
        }
    }

    private void validateRoleForUpdate(Role currentRole, Role roleRequest) {
        boolean isNameExists = checkNameExistsIgnoreCase(roleRequest.getName());

        if (isNameExists && !roleRequest.getName().equals(currentRole.getName())) {
            LOGGER.warn("Update failed: Role name '{}' already exists", roleRequest.getName());
            throw new IllegalArgumentException(
                    messageTranslator.toLocale(RoleEnum.NAME_EXISTS.getMessageKey(), roleRequest.getName())
            );
        }
    }

    private Set<String> getExistingNamesIgnoreCaseFromRoleCreateRequestList(List<RoleCreateRequest> roleCreateRequestList) {
        Set<String> setNamesToCheck = roleCreateRequestList.stream()
                .map(RoleCreateRequest::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());
        return findExistingNamesIgnoreCase(setNamesToCheck);
    }

    private void validateRoleForImport(Set<String> existingNamesIgnoreCase, RoleCreateRequest roleCreateRequest) {
        // Check name exists
        String normalizedNameLowerCase = StringUtils.trimLowerBlank(roleCreateRequest.getName());

        if (existingNamesIgnoreCase.contains(normalizedNameLowerCase)) {
            LOGGER.warn("Import failed: Role name '{}' already exists", roleCreateRequest.getName());
            throw new IllegalArgumentException(
                    messageTranslator.toLocale(RoleEnum.NAME_EXISTS.getMessageKey(), roleCreateRequest.getName())
            );
        }
    }

    private Role buildRoleFromRoleCreateRequest(RoleCreateRequest roleCreateRequest) {
        Role role = new Role();
        role.setName(roleCreateRequest.getName());
        role.setDescription(roleCreateRequest.getDescription());
        role.setActive(roleCreateRequest.getActive());
        return role;
    }

    private void validateAndSetPermissionsForRole(Role currentRole, Role roleRequest) {
        if (roleRequest.getPermissions() != null && !roleRequest.getPermissions().isEmpty()) {

            List<Long> permissionIds = roleRequest.getPermissions()
                    .stream()
                    .distinct()
                    .map(Permission::getId)
                    .toList();

            List<Permission> listPermissions = permissionRepository.findALlByIdIn(permissionIds);

            if (listPermissions.size() != permissionIds.size()) {
                Set<Long> foundIds = listPermissions.stream()
                        .map(Permission::getId)
                        .collect(Collectors.toSet());

                List<Long> notFoundIds = permissionIds.stream()
                        .filter(id -> !foundIds.contains(id))
                        .toList();

                String missingIdsString = notFoundIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ", "(", ")"));

                throw new NoSuchElementException(
                        messageTranslator.toLocale(PermissionEnum.IDS_NOT_FOUND.getMessageKey(), missingIdsString)
                );
            }

            currentRole.setPermissions(listPermissions);
        }
    }
}