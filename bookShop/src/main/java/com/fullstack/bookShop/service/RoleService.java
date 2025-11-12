package com.fullstack.bookShop.service;

import com.fullstack.bookShop.dto.pagination.ResultPaginationDTO;
import com.fullstack.bookShop.dto.role.RestRoleCreateDTO;
import com.fullstack.bookShop.dto.role.RestRoleUpdateDTO;
import com.fullstack.bookShop.dto.role.RestRoleViewDTO;
import com.fullstack.bookShop.model.Role;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface RoleService {
    Role createRole(Role role);
    Role updateRoleById(Long id, Role role);
    Role getRoleById(Long id);

    void deleteRoleById(Long id);

    boolean checkNameExistsIgnoreCase(String name);
    boolean checkIdExists(Long id);

    RestRoleCreateDTO convertRoleToRestRoleCreateDTO(Role role);
    RestRoleUpdateDTO convertRoleToRestRoleUpdateDTO(Role role);
    RestRoleViewDTO convertRoleToRestRoleViewDTO(Role role);

    ResultPaginationDTO fetchAllRoles(Specification<Role> spec, Pageable pageable);
}
