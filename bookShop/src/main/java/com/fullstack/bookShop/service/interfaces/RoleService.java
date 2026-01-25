package com.fullstack.bookShop.service.interfaces;

import com.fullstack.bookShop.dto.request.RoleCreateRequest;
import com.fullstack.bookShop.dto.response.RoleResponse;
import com.fullstack.bookShop.dto.response.imports.ImportFileResponse;
import com.fullstack.bookShop.dto.response.pagination.PaginationResponse;
import com.fullstack.bookShop.model.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface RoleService {

    PaginationResponse fetchAllRoles(Specification<Role> spec, Pageable pageable);

    Role createRole(Role roleRequest);

    Role getRoleById(Long id);

    Role getRoleByName(String name);

    Role updateRoleById(Long id, Role roleRequest);

    void deleteRoleById(Long id);

    boolean checkIdExists(Long id);

    boolean checkNameExistsIgnoreCase(String name);

    ImportFileResponse<RoleResponse> importRolesFromFiles(List<RoleCreateRequest> roleCreateRequestList);
}
